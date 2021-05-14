import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

// Implementeaza flow-ul principal al tool-ului (functia main)

public class Stegosaurus {

	public static void main(String[] args) throws IOException {
		
		String option; // poate fi -e sau -d
		
		// Pentru creare fisier debug
		// Creating a File object that represents the disk file.
        PrintStream o = new PrintStream(new File("debug/debug.txt"));
  
        // Store current System.out before assigning a new value
        @SuppressWarnings("unused")
		PrintStream console = System.out;
        
        System.setOut(o);
        
        // doar System.err va scrie, de acum, in consola
        
        
		
		/* Argumente CLI
		 * Format: java Stegosaurus -e -1 -r inputImage.bmp outputImage.bmp hello
		 * SAU java Stegosaurus -e -1 -r inputImage.bmp outputImage.bmp "-fMessageFile.txt"
		 * java Stegosaurus -d -4 -b hiddenImage.bmp 200 -f<txt>
		 * <> = argument optional (scrie rezultatul decodarii in fisier txt)
		 * 
		 */
		if (args.length > 0) {
			 if (!args[0].isEmpty()) {
			        option = args[0];
			        switch (option)		{
			        	case "--help": help(); break; 
			        	case "-e": encode(args[1], args[2], args[3], args[4], args[5]); break;
			        	case "-d": decode(args[1], args[2], args[3], args[4]); break;
			        	default: System.out.println("Introduceti o optiune valida! -e sau -d"); break;
			        }   
			 }
		}
		
		/*
		// Citire imagine
		StegoOps myImage = new StegoOps("large.bmp");
		
		// Input
		Scanner scanner = new Scanner(System.in);
		System.out.print("Mesaj de steganografiat: ");
		String string = scanner.nextLine();
		scanner.close();
		
		// Demo - mesaj mediu de scris, dar se poate observa optimizarea pe randuri in consola
		String string2 = "This is a medium message for debugging, you can use it and it works.";
		
		// Demo - mesaj foarte lung de scris
		String string3 = "This is a very long message, but can fit in a 400 x 400 image: "
				+ "Lorem Ipsum is simply dummy text of the printing and typesetting industry. "
				+ "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, "
				+ "when an unknown printer took a galley of type and scrambled it to make a type specimen book. ";
		
		// Steganografiere imagine si scriere
		myImage.lsb1(string2, "lsb1.bmp");
		
		myImage.decodeLsb1(40);
		
		// NOTA: se poate utiliza un singur tip de steganografiere la o rulare main
		// (altfel nu merge decodarea)
		// myImage.lsb2(string3, "lsb2.bmp");
		
		// Decodare mesaj -> nu prea merge inca pt alte imagini inafara de dog.bmp...
		// myImage.decodeLsb1(string.length());
		 * */
		
	}
	
	// Optiunea -e
	// Method = numarul de lsb de ascuns
	public static void encode(String method, String channel, String imageName, String outputName, String message) throws IOException	{
		String type = "notype"; // folosit la tratare fisiere  -> daca las null imi da exception!
		String file = "nofile";
		
		// Verificare metoda este data corect
		if (method.isEmpty())
			System.out.println("Intoduceti metoda de staganografiat!");
		else
		if (!method.contains("-"))
			System.out.println("Folositi sintaxa corecta pentru metoda LSB!");
		
		
		// Verificare numele imaginii exista si este corect
		if (imageName.isEmpty())
			System.out.println("Introduceti numele imaginii! (image.bmp)");
		else
		if (!imageName.contains(".bmp"))
			System.out.println("Introduceti o imagine cu extensia .bmp!");
		else
		
		// Verificare tip fisier (message)
		if (message.startsWith("-f"))	{
			System.out.println("Message starts with -f");
			
			// Tratare fisiere de codat
			
			// Preluare extensie fisier (rezultat "txt")
			int index = message.lastIndexOf('.');
			  if (index > 0) {
			      type = message.substring(index + 1);
			  }
			System.out.println("Tip fisier: " + type); // debug
			
			// Preluare nume fisier
			file = message.replace("-f", "");
			System.out.println("Nume fisier: " + file); // debug
			
			StegoOps myImage = new StegoOps(imageName);
			System.err.println("Canal de culoare selectat: " + channel);
			
			if (type.equals("txt"))	{
				switch (method)	{
					case "-1": myImage.lsb1(channel, file, outputName, type); break;
					case "-2": myImage.lsb2(channel, file, outputName, type); break;
					case "-3": myImage.lsb3(channel, file, outputName, type); break;
					case "-4": myImage.lsb4(channel, file, outputName, type); break;
					case "-auto": myImage.lsbauto(channel, file, outputName, type); break;
					default: System.err.println("Introduceti un caz valid!");
				}
			}
		}
		else	{
			// Tratare mesaj de codat
			StegoOps myImage = new StegoOps(imageName);
			System.err.println("Canal de culoare selectat: " + channel);
			switch (method)	{
				case "-1": myImage.lsb1(channel, message, outputName, type); break;
				case "-2": myImage.lsb2(channel, message, outputName, type); break;
				case "-3": myImage.lsb3(channel, message, outputName, type); break;
				case "-4": myImage.lsb4(channel, message, outputName, type); break;
				case "-auto": myImage.lsbauto(channel, file, outputName, type); break;
				default: System.err.println("Introduceti un caz valid!");
			}
		}
		
		
	}
	
	// Optiunea -d
	public static void decode(String method, String channel, String imageName, String stringLenght) throws IOException	{
		
		// Verificare metoda este data corect
		if (method.isEmpty())
			System.err.println("Intoduceti metoda de extras mesajul!");
		else
		if (!method.contains("-"))
			System.err.println("Folositi sintaxa corecta pentru metoda LSB!");
		
		// Verificare numele imaginii exista si este corect
		if (imageName.isEmpty())
			System.err.println("Introduceti numele imaginii! (image.bmp)");
		else
		if (!imageName.contains(".bmp"))
			System.err.println("Introduceti o imagine cu extensia .bmp!");
		
		int intValue = 0;
		
		// Verificare stringLenght este format de numar
		try {
		    intValue = Integer.parseInt(stringLenght);
		} catch (NumberFormatException e) {
		    System.err.println("Input String cannot be parsed to Integer.");
		}
		if (intValue == 0)
			System.err.println("A aparut o eroare la string lenght!");
		
		// asignare imagine de decodat
		StegoOps myImage = new StegoOps(imageName);
		
		System.err.println("Canal de culoare selectat: " + channel);
		
		switch (method)	{
			case "-1": myImage.decodeLsb1(channel, intValue); break;
			case "-2": myImage.decodeLsb2(channel, intValue); break;
			case "-3": myImage.decodeLsb3(channel, intValue); break;
			case "-4": myImage.decodeLsb4(channel, intValue); break;
			default: System.err.println("Introduceti un caz valid!"); break;
		}
	}
	
	public static void help()	{
		System.err.println("\nStegosaurus, pagina de help\n=====================================");
		System.err.println("Codare / decodare de mesaje steganografice pe imagini tip .bmp prin metoda LSB.\n");
		System.err.println("Sintaxa\n------------------------------\n");
		System.err.println("1. Compilare cu javac (JDK) - optional\n");
		System.err.println("$ javac Stegosaurus.java\n");
		System.err.println("2. Codare imagine\n");
		System.err.println("$ java Stegosaurus -e -<numar LSB> -<canal culoare> <imagine intrare> <imagine iesire>"
				+ "<mesaj>\n");
		System.err.println("Semnificatia parametrilor:\r\n" + 
				"<numar LSB> - numarul de LSB pe care se va scrie mesajul; poate avea valorile 1, 2, 3, 4 "
				+ "\nsau auto (selectare automata a dimensiunii optime);\r\n" + 
				"<canal culoare> - canalul de culoare pe care se va ascunde mesajul; "
				+ "\npoate avea valorile r, g, b;\r\n" + 
				"<imagine de intrare> - numele imaginii originale, in care se doreste ascunderea mesajului;\r\n" + 
				"<imagine de ieșire> - numele imaginii de iesire, ce va contine mesajul ascuns;\r\n" + 
				"<mesaj> - mesajul de ascuns; poate fi preluat si dintr-un fisier tip text, "
				+ "\nfolosind prefixul -s (exemplu: -fMesaj.txt).\r\n" + 
				"");
		System.err.println("\n3. Decodare imagine\n");
		System.err.println("$ java Stegosaurus -d -<numar LSB> -<canal culoare> <imagine de decodat>"
				+ "<dimensiune mesaj>\n");
		System.err.println("Semnificatia parametrilor:\r\n" + 
				"<imagine de decodat> - numele imaginii din care se va recupera mesajul;\r\n" + 
				"<dimensiune mesaj> - dimensiunea mesajului, in numar de caractere.\r\n" + 
				"");
		System.err.println("\n4. Pagina help\n");
		System.err.println("$ java Stegosaurus --help\n");
		System.err.println("Utilizare .exe\n------------------------------\n");
		System.err.println("Pentru a facilita accesul la aplicatie, aceasta poate fi rulata si independent\n"
				+ " de JRE (Java Runtime Environment) pe sistemul de operare Windows, cu ajutorul fisierului\n"
				+ " Stegosaurus.exe. Se va inlocui java Stegosaurus cu ./Stegosaurus.exe in apelul\n"
				+ " acesteia din linie de comanda.\r\n" + 
				"");
		
	}
	
}
