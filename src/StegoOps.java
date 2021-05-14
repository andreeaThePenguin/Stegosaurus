import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// Clasa StegoOps va implementa functiile steganografice, peste wrapper-ul de imagine ImageOps

public class StegoOps extends ImageOps {
	
	// Raster pentru scriere matrice noua imagine
	WritableRaster newRaster;
	
	// Componente canale imagine
	private int imageR[][];
	private int imageG[][];
	private int imageB[][];
	
	// Atribut intern pentru codare LSB unde nr biti > 1
	private byte sum = 0;
	
	// Constructor -> in plus, construieste matricele de pixeli a imaginii pentru RGB
	public StegoOps(String name) throws IOException {
		super(name);
		
		this.imageR = new int[this.getWidth()][this.getHeight()];
		this.imageG = new int[this.getWidth()][this.getHeight()];
		this.imageB = new int[this.getWidth()][this.getHeight()];
		
		for (int j = 0; j < this.getHeight(); j++)
			for (int i = 0; i < this.getWidth(); i++)	{
				imageR[i][j] = this.getRaster().getSample(i, j, 0);
				imageG[i][j] = this.getRaster().getSample(i, j, 1);
				imageB[i][j] = this.getRaster().getSample(i, j, 2);
			}
	}
	
	/*
	 * Ascundere mesaj cu LSB + reconstructie imagine
	 * channel = -r, -g, -b
	 * Urmatoarele 4 functii sunt asemanatoare, insa
	 * pentru claritate au fost implementate separat.
	 * Ele efectueaza, pe un numar de LSB selectati,
	 * CODAREA MESAJULUI
	 * 
	 * String testStr are dublu sens, in functie de
	 * String type: string-ul de codat SAU numele 
	 * fisierului de codat.
	 */
	
	public void lsb1(String channel, String testStr, String filename, String type) throws IOException {
		newRaster = cm.createCompatibleWritableRaster(width, height);
		
		System.out.println("lsb1: testStr: " + testStr);
		
		// string
		int strLen = testStr.length();
		boolean [] msgBits = stringToBitsMatrix(testStr); // boolean [strLen * 8]
				
		System.out.println("Detected type: " + type);
		
		// Tratare cazuri particulare de fisiere:
		
		if (type.equals("txt"))	{
			String myFile = testStr;
			byte[] bytes = Files.readAllBytes(Paths.get(myFile));
			String content = new String(bytes);
			testStr = content;
			System.out.println("Mesaj de scris in imagine: " + testStr);
			strLen = testStr.length();
			msgBits = stringToBitsMatrix(testStr);
		}
		
		// Pregatire mesaj pentru incorporare in imagine.
		// Optimizat doar dupa numarul de coloane (nu este cea mai "ideala)
		int sRows = (8 * strLen) / this.getHeight() + 1;
		int sCols = this.getHeight();
		boolean [][] sectionedMsg = new boolean[sRows][sCols];
		
		// Mesaje debug
		System.out.println("lsb1: Image width / height " + this.getWidth() + "  " + this.getHeight());
		System.out.println("lsb1: Number of bits: " + ((8 * strLen) / this.getHeight()));
		System.out.println("lsb1: sectionedMsg init size assigned: " + sRows + "  " +
				sCols);
		
		for (int i = 0; i < 8 * strLen; i++)
			sectionedMsg[i / this.getHeight()][i % this.getHeight()] = msgBits[i];
		
		// Incorporare biti in matrice
		for (int j = 0; j < sectionedMsg[0].length; j++) // corespunde this.getHeight()
			for (int i = 0; i < sectionedMsg.length; i++)	{ // corespunde this.getWidth()
				if (channel.equals("-r"))		{
					imageR[i][j] = imageR[i][j]>>1;
					imageR[i][j] = imageR[i][j]<<1;
					if (sectionedMsg[i][j] == true)	{
						imageR[i][j]++;
					}
				}
				if (channel.equals("-g"))		{
					imageG[i][j] = imageG[i][j]>>1;
					imageG[i][j] = imageG[i][j]<<1;
					if (sectionedMsg[i][j] == true)	{
						imageG[i][j]++;
					}
				}
				if (channel.equals("-b"))		{
					imageB[i][j] = imageB[i][j]>>1;
					imageB[i][j] = imageB[i][j]<<1;
					if (sectionedMsg[i][j] == true)	{
						imageB[i][j]++;
					}
				}	
			}
		
		// loop scriere newRaster
		for (int j = 0; j < this.getHeight(); j++)
			for (int i = 0; i < this.getWidth(); i++)	{
				newRaster.setSample(i, j, 0, imageR[i][j]);
				newRaster.setSample(i, j, 1, imageG[i][j]);
				newRaster.setSample(i, j, 2, imageB[i][j]);
						}
		reconstructImage(filename);
	}
	
	// Ascundere mesaj cu LSB2 + reconstructie imagine
	public void lsb2(String channel, String testStr, String filename, String type) throws IOException {
		newRaster = cm.createCompatibleWritableRaster(width, height);
		
		// Tratare cazuri particulare de fisiere:
			if (type.equals("txt"))	{
				String myFile = testStr;
				byte[] bytes = Files.readAllBytes(Paths.get(myFile));
				String content = new String(bytes);
				testStr = content;
				System.out.println("Mesaj de scris in imagine: " + testStr);
				}
		
		// Tratare string
		int strLen = testStr.length();
		boolean [] msgBits = stringToBitsMatrix(testStr); // boolean [strLen * 8]
			
		// Pregatire mesaj pentru incorporare in imagine.
		// Optimizat doar dupa numarul de coloane (nu este cea mai "ideala)
		
		int sRows = ((8 / 2) * strLen) / this.getHeight() + 1; // 8 / (nr biti LSB)
		int sCols = this.getHeight();
		byte [][] sectionedMsg = new byte[sRows][sCols]; // fiecar eelement va contine cate 2 biti
			
		// debug
		System.out.println("lsb2: width / height" + this.getWidth() + "  " + this.getHeight());
		System.out.println("lsb2: sectionedMsg init size assigned: " + sRows + "  " +
				sCols);
		for (int i = 0; i < 8 * strLen; i=i+2) 	{
			sum = 0;
			System.out.println("lsb2: Max i: " + 8*strLen + " current i: " + i);
			if (msgBits[i])
				sum+=2;
			if (msgBits[i + 1])
				sum++;
			sectionedMsg[(i / 2) / this.getHeight()][(i / 2) % this.getHeight()] = sum; // (i / nr biti)
				
			// debug
			System.out.println("lsb2: sum value: " + sum);
			System.out.println("lsb2: sectionedMsg value: " + sectionedMsg[(i / 2) / this.getHeight()][(i / 2) % this.getHeight()]);
		}
				
		// Incorporare biti in matrice
		for (int j = 0; j < sectionedMsg[0].length; j++) // corespunde this.getHeight()
			for (int i = 0; i < sectionedMsg.length; i++)	{ // corespunde this.getWidth()
				if (channel.equals("-r"))		{
					imageR[i][j] = imageR[i][j]>>2;
					imageR[i][j] = imageR[i][j]<<2;
					imageR[i][j] += sectionedMsg[i][j];
				}
				if (channel.equals("-g"))		{
					imageG[i][j] = imageG[i][j]>>2;
					imageG[i][j] = imageG[i][j]<<2;
					imageG[i][j] += sectionedMsg[i][j];
				}
				if (channel.equals("-b"))		{
					imageB[i][j] = imageB[i][j]>>2;
					imageB[i][j] = imageB[i][j]<<2;
					imageB[i][j] += sectionedMsg[i][j];
				}
				
				// debug
				if (sectionedMsg[i][j] != 0)
					System.out.println("lsb2: Pixel " + i + ", " + j + " of value: " + imageR[i][j] + 
							" was modified with: " + sectionedMsg[i][j]);
			}
		
			// loop scriere newRaster
			for (int j = 0; j < this.getHeight(); j++)
				for (int i = 0; i < this.getWidth(); i++)	{
					newRaster.setSample(i, j, 0, imageR[i][j]);
					newRaster.setSample(i, j, 1, imageG[i][j]);
					newRaster.setSample(i, j, 2, imageB[i][j]);
				}
			reconstructImage(filename);
		}
	
	// Ascundere mesaj cu LSB3 + reconstructie imagine
	public void lsb3(String channel, String testStr, String filename, String type) throws IOException {
		newRaster = cm.createCompatibleWritableRaster(width, height);
		
		// Tratare cazuri particulare de fisiere:
		if (type.equals("txt"))	{
			String myFile = testStr;
			byte[] bytes = Files.readAllBytes(Paths.get(myFile));
			String content = new String(bytes);
			testStr = content;
			System.out.println("Mesaj de scris in imagine: " + testStr);
		}
		
		// Eliminare risc erori (3 nu e divizibil cu 2)
		testStr = testStr + " ";
		
		int strLen = testStr.length();
		boolean [] msgBits = stringToBitsMatrix(testStr); // boolean [strLen * 8]
			
		// Pregatire mesaj pentru incorporare in imagine.
		// Optimizat doar dupa numarul de coloane (nu este cea mai "ideala)
		
		int sRows = (8 * strLen) / this.getHeight() + 1;
		int sCols = this.getHeight();
		byte [][] sectionedMsg = new byte[sRows][sCols];
			
		// debug
		System.out.println("lsb3: width / height" + this.getWidth() + "  " + this.getHeight());
		System.out.println("lsb3: sectionedMsg init size assigned: " + sRows + "  " +
				sCols);
		for (int i = 0; i < 8*strLen - 4; i=i+3) 	{ 
			sum = 0; 
			System.out.println("lsb3: Max i: " + (8*strLen - 1) + " current i: " + i);
			if (msgBits[i])
				sum+=4;
			if (msgBits[i + 1])
				sum+=2;
			if (msgBits[i + 2])
				sum++;
			sectionedMsg[(i / 3) / this.getHeight()][(i / 3) % this.getHeight()] = sum;
			
			// debug
			System.out.println("lsb3: sum value: " + sum);
			System.out.println("lsb3: sectionedMsg value: " + sectionedMsg[(i / 3) / this.getHeight()][(i / 3) % this.getHeight()]);
		}

		// Incorporare biti in matrice
		for (int j = 0; j < sectionedMsg[0].length; j++) // corespunde this.getHeight()
			for (int i = 0; i < sectionedMsg.length; i++)	{ // corespunde this.getWidth()
				if (channel.equals("-r"))		{
					imageR[i][j] = imageR[i][j]>>3;
					imageR[i][j] = imageR[i][j]<<3;
					imageR[i][j] += sectionedMsg[i][j];
				}
				if (channel.equals("-g"))		{
					imageG[i][j] = imageG[i][j]>>3;
					imageG[i][j] = imageG[i][j]<<3;
					imageG[i][j] += sectionedMsg[i][j];
				}
				if (channel.equals("-b"))		{
					imageB[i][j] = imageB[i][j]>>3;
					imageB[i][j] = imageB[i][j]<<3;
					imageB[i][j] += sectionedMsg[i][j];
					System.out.println("i: " + i + " j: " + j);
				}
					
				// debug
				if (sectionedMsg[i][j] != 0)
					System.out.println("lsb3: Pixel " + i + ", " + j + " of value: " + imageR[i][j] + 
							" was modified with: " + sectionedMsg[i][j]);
			}
			
		// loop scriere newRaster
		for (int j = 0; j < this.getHeight(); j++)
			for (int i = 0; i < this.getWidth(); i++)	{
				newRaster.setSample(i, j, 0, imageR[i][j]);
				newRaster.setSample(i, j, 1, imageG[i][j]);
				newRaster.setSample(i, j, 2, imageB[i][j]);
			}
		reconstructImage(filename);
	}
		
	// Ascundere mesaj cu LSB4 + reconstructie imagine
	public void lsb4(String channel, String testStr, String filename, String type) throws IOException {
		newRaster = cm.createCompatibleWritableRaster(width, height);
		
		// Tratare cazuri particulare de fisiere:
		if (type.equals("txt"))	{
			String myFile = testStr;
			byte[] bytes = Files.readAllBytes(Paths.get(myFile));
			String content = new String(bytes);
			testStr = content;
			System.out.println("lsb4: Mesaj de scris in imagine: " + testStr);
		}
			
		int strLen = testStr.length();
		boolean [] msgBits = stringToBitsMatrix(testStr);
			
		// Pregatire mesaj pentru incorporare in imagine.
		// Optimizat doar dupa numarul de coloane (nu este cea mai "ideala)
		
		int sRows = ((8 / 4) * strLen) / this.getHeight() + 1; // 8 / nr biti LSB
		int sCols = this.getHeight();
		byte [][] sectionedMsg = new byte[sRows][sCols];
			
		// debug
		System.out.println("lsb4: width / height" + this.getWidth() + "  " + this.getHeight());
		System.out.println("lsb4: sectionedMsg init size assigned: " + sRows + "  " +
				sCols);
		for (int i = 0; i < 8 * strLen; i=i+4) 	{
			sum = 0;
			if (msgBits[i])
				sum+=8;
			if (msgBits[i + 1])
				sum+=4;
			if (msgBits[i + 2])
				sum+=2;
			if (msgBits[i + 3])
				sum++;
			sectionedMsg[(i / 4) / this.getHeight()][(i / 4) % this.getHeight()] = sum; // (i / nr biti)
			
			// debug
			System.out.println("lsb4: sum value: " + sum);
			System.out.println("lsb4: sectionedMsg value: " + sectionedMsg[(i / 4) / this.getHeight()][(i / 4) % this.getHeight()]);
		}
			
		// Incorporare biti in matrice
		for (int j = 0; j < sectionedMsg[0].length; j++) // corespunde this.getHeight()
			for (int i = 0; i < sectionedMsg.length; i++)	{ // corespunde this.getWidth()
				if (channel.equals("-r"))		{
					imageR[i][j] = imageR[i][j]>>4;
					imageR[i][j] = imageR[i][j]<<4;
					imageR[i][j] += sectionedMsg[i][j];
				}
				if (channel.equals("-g"))		{
					imageG[i][j] = imageG[i][j]>>4;
					imageG[i][j] = imageG[i][j]<<4;
					imageG[i][j] += sectionedMsg[i][j];
				}
				if (channel.equals("-b"))		{
					imageB[i][j] = imageB[i][j]>>4;
					imageB[i][j] = imageB[i][j]<<4;
					imageB[i][j] += sectionedMsg[i][j];
				}
				
				// debug
				if (sectionedMsg[i][j] != 0)
					System.out.println("lsb4: Pixel " + i + ", " + j + " of value: " + imageR[i][j] + 
							" was modified with: " + sectionedMsg[i][j]);
			}
		
		// loop scriere newRaster
		for (int j = 0; j < this.getHeight(); j++)
			for (int i = 0; i < this.getWidth(); i++)	{
				newRaster.setSample(i, j, 0, imageR[i][j]);
				newRaster.setSample(i, j, 1, imageG[i][j]);
				newRaster.setSample(i, j, 2, imageB[i][j]);
			}
		reconstructImage(filename);
	}
		
	// stabilire automata a nr de biti pe care se poate coda mesajul
	public void lsbauto(String channel, String testStr, String filename, String type) throws IOException {
		
		System.out.println("lsbauto: Tip fisier: " + type);
		
		String testStr2 = testStr;
		@SuppressWarnings("unused")
		int strLen = testStr2.length();
		
		// Tratare fisier txt
		if (type.equals("txt"))	{
			String myFile = testStr2;
			byte[] bytes = Files.readAllBytes(Paths.get(myFile));
			String content = new String(bytes);
			testStr2 = content;
			strLen = testStr2.length();
		}
		
		int imgDim = this.getHeight() * this.getWidth();
		int strDim = testStr2.length() * 8; // dimensiunea in biti
		
		if (strDim < imgDim)	{
			System.err.println("Mesajul va fi scris pe 1 bit");
			this.lsb1(channel, testStr, filename, type);
			}
		else
		if (strDim < 2 * imgDim)	{
			System.err.println("Mesajul va fi scris pe 2 biti");
			this.lsb2(channel, testStr, filename, type);
			}
		else
		if (strDim < 4 * imgDim)	{
			System.err.println("Mesajul va fi scris pe 4 biti");
			this.lsb4(channel, testStr, filename, type);
			}
		else
			System.err.println("Mesajul este prea mare / imaginea este prea mica!");
		
		}

	/*
	 * Descoperire mesaj cu LSB si afisare in terminal
	 * channel = -r, -g, -b
	 * Urmatoarele 4 functii sunt asemanatoare, insa
	 * pentru claritate au fost implementate separat.
	 * Ele efectueaza, pe un numar de LSB selectati +
	 * o lungime data a mesajului
	 * DECODAREA MESAJULUI
	 * 
	 * Pro tip: daca nu se cunoaste lungimea mesajului se poate da
	 * un numar suficient de mare ca input.
	 */
	public void decodeLsb1(String channel, int strSize) throws IOException	{
		
		// pastreaza valorile LSB din toata imaginea
		byte [] currentBit = new byte [this.getWidth()*this.getHeight()];
		
		for (int j = 0; j < this.getHeight(); j++)
			for (int i = 0; i < this.getWidth(); i++)	{
				if (channel.equals("-r"))
					currentBit[i*height + j] = checkBit(imageR[i][j], 0); 
				if (channel.equals("-g"))
					currentBit[i*height + j] = checkBit(imageG[i][j], 0); 
				if (channel.equals("-b"))
					currentBit[i*height + j] = checkBit(imageB[i][j], 0); 
			}
		
		// prelucrez doar bitii de pe lungimea Stringului
		byte [] bitValues = new byte[8];
		char [] decodedChars = new char[strSize];
		for (int i = 0; i < strSize * 8; i++)	{
			System.out.println("decodeLsb1: My currentBit: " + currentBit[i]);
			if (i % 8 == 0)	{
				for (int k = 0; k < 8; k++)	{
					bitValues[k] = currentBit[k + i];
				}
				System.out.println("decodeLsb1: Entered arraycopy at i = " + i);
				System.out.println("decodeLsb1: Bitvalues:");
				for (int j = 0; j < 8; j++)	{
					System.out.println(bitValues[j]);
				}
			}
			if (i % 8 == 0)	{
				char c = byteToAscii(bitValues);
				System.out.println("decodeLsb1: My char: " + c);
				decodedChars[i / 8] = c;
			}	
		}
		
		// convert char to string and return / print
		String decodedString = new String(decodedChars);
		System.out.println("decodeLsb1: Decoded: " + decodedString);
		System.err.println("Mesajul decodat \n---------------------\n" + decodedString);
		
		// scriere rezultat in fisier text
		File output = new File("decodedOutput.txt");
		FileWriter writer = new FileWriter(output);
		writer.write(decodedString);
		writer.flush();
		writer.close();
	}
	
	public void decodeLsb2(String channel, int strSize) throws IOException	{
		
		System.out.println("decodeLsb2: Got height: " + this.getHeight());
		System.out.println("decodeLsb2: Got width: " + this.getWidth());
		
		// pastreaza valorile LSB din toata imaginea
		byte [] currentBit = new byte [this.getWidth()*this.getHeight()*2]; // * nr de lsb
		for (int j = 0; j < this.getHeight(); j++)
			for (int i = 0; i < this.getWidth(); i++)	{
				if (channel.equals("-r"))	{
					currentBit[(i*this.getHeight()+j)*2] = checkBit(imageR[i][j], 1); 
					currentBit[(i*this.getHeight()+j)*2+1] = checkBit(imageR[i][j], 0); 
				}
				if (channel.equals("-g"))	{
					currentBit[(i*this.getHeight()+j)*2] = checkBit(imageG[i][j], 1); 
					currentBit[(i*this.getHeight()+j)*2+1] = checkBit(imageG[i][j], 0); 
				}
				if (channel.equals("-b"))	{
					currentBit[(i*this.getHeight()+j)*2] = checkBit(imageB[i][j], 1); 
					currentBit[(i*this.getHeight()+j)*2+1] = checkBit(imageB[i][j], 0);
				}
			}
		
		byte [] bitValues = new byte[8];
		char [] decodedChars = new char[strSize];
		for (int i = 0; i < strSize * 8; i++)	{
			if (i % 8 == 0)	{
				for (int k = 0; k < 8; k++)	{
					bitValues[k] = currentBit[k + i];
				}
			}
			if (i % 8 == 0)	{
				char c = byteToAscii(bitValues);
				decodedChars[i / 8] = c;
			}	
		}
		String decodedString = new String(decodedChars);
		System.out.println("decodeLsb2: Decoded: " + decodedString);
		System.err.println("Mesajul decodat \n---------------------\n" + decodedString);
		
		// scriere in fisier text
		File output = new File("decodedOutput.txt");
		FileWriter writer = new FileWriter(output);
		writer.write(decodedString);
		writer.flush();
		writer.close();
	}
	
	public void decodeLsb3(String channel, int strSize) throws IOException	{ 
		
		System.out.println("decodeLsb3: Got height: " + this.getHeight());
		System.out.println("decodeLsb3: Got width: " + this.getWidth());
		
		byte [] currentBit = new byte [this.getWidth() * this.getHeight() * 3];
		for (int j = 0; j < this.getHeight(); j++)
			for (int i = 0; i < this.getWidth(); i++)	{
				if (channel.equals("-r"))	{
					currentBit[(i*this.getHeight()+j)*3] = checkBit(imageR[i][j], 2);
					currentBit[(i*this.getHeight()+j)*3+1] = checkBit(imageR[i][j], 1); 
					currentBit[(i*this.getHeight()+j)*3+2] = checkBit(imageR[i][j], 0);
				}
				if (channel.equals("-g"))	{
					currentBit[(i*this.getHeight()+j)*3] = checkBit(imageG[i][j], 2);
					currentBit[(i*this.getHeight()+j)*3+1] = checkBit(imageG[i][j], 1);
					currentBit[(i*this.getHeight()+j)*3+2] = checkBit(imageG[i][j], 0);
				}
				if (channel.equals("-b"))	{
					currentBit[(i*this.getHeight()+j)*3] = checkBit(imageB[i][j], 2);
					currentBit[(i*this.getHeight()+j)*3+1] = checkBit(imageB[i][j], 1);
					currentBit[(i*this.getHeight()+j)*3+2] = checkBit(imageB[i][j], 0);
				}
			}
		
		byte [] bitValues = new byte[8];
		char [] decodedChars = new char[strSize];
		for (int i = 0; i < strSize * 8; i++)	{
			System.out.println("decodeLsb3: My currentBit: " + currentBit[i] + "i: " + i);
			if (i % 8 == 0)	{
				for (int k = 0; k < 8; k++)	{
					bitValues[k] = currentBit[k + i];
				}
				System.out.println("decodeLsb3: Entered arraycopy at i = " + i);
				System.out.println("decodeLsb3: Bitvalues:");
				for (int j = 0; j < 8; j++)	{
					System.out.println(bitValues[j]);
				}
			}
			if (i % 8 == 0)	{
				char c = byteToAscii(bitValues);
				System.out.println("decodeLsb3: My char: " + c);
				decodedChars[i / 8] = c;
			}	
		}
		String decodedString = new String(decodedChars);
		System.out.println("decodeLsb3: Decoded: " + decodedString);
		System.err.println("Mesajul decodat \n---------------------\n" + decodedString);
		
		// scriere in fisier text
		File output = new File("decodedOutput.txt");
		FileWriter writer = new FileWriter(output);
		writer.write(decodedString);
		writer.flush();
		writer.close();
	}
	
	public void decodeLsb4(String channel, int strSize) throws IOException	{ 
		
		System.out.println("decodeLsb4: Got height: " + this.getHeight());
		System.out.println("decodeLsb4: Got width: " + this.getWidth());
		
		byte [] currentBit = new byte [this.getWidth()*this.getHeight()*4];
		for (int j = 0; j < this.getHeight(); j++)
			for (int i = 0; i < this.getWidth(); i++)	{
				if (channel.equals("-r"))	{
					currentBit[(i*this.getHeight()+j)*4] = checkBit(imageR[i][j], 3); 
					currentBit[(i*this.getHeight()+j)*4+1] = checkBit(imageR[i][j], 2); 
					currentBit[(i*this.getHeight()+j)*4+2] = checkBit(imageR[i][j], 1);
					currentBit[(i*this.getHeight()+j)*4+3] = checkBit(imageR[i][j], 0);
				}
				if (channel.equals("-g"))	{
					currentBit[(i*this.getHeight()+j)*4] = checkBit(imageG[i][j], 3);
					currentBit[(i*this.getHeight()+j)*4+1] = checkBit(imageG[i][j], 2);
					currentBit[(i*this.getHeight()+j)*4+2] = checkBit(imageG[i][j], 1);
					currentBit[(i*this.getHeight()+j)*4+3] = checkBit(imageG[i][j], 0);
				}
				if (channel.equals("-b"))	{
					currentBit[(i*this.getHeight()+j)*4] = checkBit(imageB[i][j], 3);
					currentBit[(i*this.getHeight()+j)*4+1] = checkBit(imageB[i][j], 2);
					currentBit[(i*this.getHeight()+j)*4+2] = checkBit(imageB[i][j], 1);
					currentBit[(i*this.getHeight()+j)*4+3] = checkBit(imageB[i][j], 0);
				}
			}
		
		byte [] bitValues = new byte[8];
		char [] decodedChars = new char[strSize];
		for (int i = 0; i < strSize * 8; i++)	{
			System.out.println("decodeLsb4: My currentBit: " + currentBit[i] + "i: " + i);
			if (i % 8 == 0)	{
				for (int k = 0; k < 8; k++)	{
					bitValues[k] = currentBit[k + i];
				}
				System.out.println("decodeLsb4: Entered arraycopy at i = " + i);
				System.out.println("decodeLsb4: Bitvalues:");
				for (int j = 0; j < 8; j++)	{
					System.out.println(bitValues[j]);
				}
			}
			if (i % 8 == 0)	{
				char c = byteToAscii(bitValues); 
				System.out.println("decodeLsb4: My char: " + c);
				decodedChars[i / 8] = c;
			}	
		}
		// convert char to string and return / print
		String decodedString = new String(decodedChars);
		System.out.println("decodeLsb4: Decoded: " + decodedString);
		System.err.println("Mesajul decodat \n---------------------\n" + decodedString);
		
		// scriere in fisier text
		File output = new File("decodedOutput.txt");
		FileWriter writer = new FileWriter(output);
		writer.write(decodedString);
		writer.flush();
		writer.close();
	}
	
	/*
	 * Urmatoarele functii sunt INTERNE,
	 * vitale in codarea / decodarea mesajului.
	 * Ele se refera la usurarea lucrului cu bitii mesajului,
	 * si NU SUNT IN TOTALITATE originale.
	 */
	
	// convertire vector de 8 bytes (in care se afla, de fapt, biti) in litera corespunzatoare
	public char byteToAscii(byte [] bitValues)	{
		int powerOf2 = 128; // puterile lui 2
		int sum = 0; // codul ascii final al caracterului
		for (int i = 0; i < 8; i++)	{
			sum = sum + bitValues[i] * powerOf2;
			System.out.println("My in fun sum: " + sum);
			powerOf2 = powerOf2>>1; // div 2
		}
		return (char) sum;
	}
	
	// Reconstruire imagine din obiect WritableRaster
	public void reconstructImage(String filename)	{
		BufferedImage imageO1 = new BufferedImage(cm, newRaster, cm.isAlphaPremultiplied(), null);
		ImageOps imageO2 = new ImageOps(imageO1, filename);
		// scriere imagine in fisier
		try	{
			imageO2.setImage(filename);
		}
		catch (IOException e)	{
			e.printStackTrace();
		}
		
	}
	
	// Schimba nbit din numar cu opusul lui
	public static int change(int num, int nbit) {
	    return num ^ (1 << nbit);
	}
	
	// Convertire un numar <number> de <lenght> biti in echivalentul sau binar
	public static boolean[] toBinary(int number, int lenght) {
	    boolean[] ret = new boolean[lenght];
	    for (int i = 0; i < lenght; i++) {
	        ret[lenght - 1 - i] = (1 << i & number) != 0;
	    }
	    return ret;
	}
	
	// Convertire string to ascii array
	public static int[] stringToAscii(String str)	{
		int strLen = str.length();
		int [] ascii = new int[strLen];
		for (int i = 0; i < strLen; i++)	{
			char c = str.charAt(i);
			ascii[i] = (int) c;
		}
		return ascii;
		
	}
	
	// Creare matrice biti 0/1 ai mesajului String
	public static boolean[] stringToBitsMatrix(String str)	{
		int [] ascii = stringToAscii(str);
		int len = str.length();
		boolean [] msgBits = new boolean[len * 8]; // bitii 0 / 1 ai intreg Stringului
		for (int i = 0; i < len; i++)	{
			// debug
			System.out.println("Ascii nums\n");
			System.out.println(ascii[i]);
			
			boolean [] asciiBits = new boolean[8]; // true / false
			asciiBits = toBinary(ascii[i], 8);
			
			System.out.println("Ascii bits\n"); // debug
			for (int j = 0; j < 8; j++)	{
				System.out.println(asciiBits[j]); // debug
				msgBits[i * 8 + j] = asciiBits[j]; 
			}
		}
		return msgBits;
	}
	
	// Verifica bitul <bit> (7:0) al unui numar <num> dat
		public byte checkBit(int num, int bit)	{
			return (byte) ((byte) (num >>> bit) & 1);
		}
}
