import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

// Operatii de baza de citire / scriere imagine si
// de reprezentare a acesteia in componente utile
// mai tarziu.

public class ImageOps {
	
	// Componente de baza
	private File file = null; // deschidere fisier imagine
	private BufferedImage image = null;
	String imageName; // numele complet al imaginii pentru stegano
	
	// Componente BufferedImage
	protected Raster raster;
	protected ColorModel cm;
	protected int width;
	protected int height;
	
	// Constructor = citire imagine
	public ImageOps(String name) throws IOException	{
		this.imageName = name;
		file = new File("pictures/" + imageName);
		try	{
		image = ImageIO.read(file);
		} catch (IOException e)	{
			System.out.println("Exceptie!\n ");
			e.printStackTrace();
		}
		
		// Setare componente des utilizate
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.raster = image.getRaster();
		this.cm = image.getColorModel();
	}
	
	// Constructor creare imagine pe baza BufferedImage (util in StegoOps)
	ImageOps(BufferedImage imageInput, String name)	{
		this.image = imageInput;
		this.imageName = name;
	    
		// Setare componente des utilizate
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.raster = image.getRaster();
		this.cm = image.getColorModel();
	}
	
	// Scriere imagine
	// NU scrie imaginea steganografiata, ci pe cea originala!
	// Pentru imaginea steganografiata, scrierea ei e integrata in procesare
	public void setImage(String name) throws IOException	{
		try		{
		    file = new File("pictures/" + name);  
		    ImageIO.write(image, "bmp", file);
		}
		catch(IOException e)	{
		      System.out.println("Exceptie!\n");
		      e.printStackTrace();
		}
	}
	
	// Getteri accesibili din exterior
	public int getWidth()	{
		return this.width;
	}
	
	public int getHeight()	{
		return this.height;
	}
	
	public Raster getRaster()	{
		return this.raster;
	}
	
	public ColorModel getColorModel()	{
		return this.cm;
	}
	
	public BufferedImage getBufferedImage()	{
		return this.image;
	}
	
}
