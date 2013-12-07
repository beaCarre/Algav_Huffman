package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {
	public static void main(String[] args) throws IOException {

		File f = new File("test.vbgz");

		Huffman.compression("abracadabra", new FileOutputStream(f));

		System.out.println(Huffman.decompression(new FileInputStream(new File("test.vbgz"))));

	}
}
