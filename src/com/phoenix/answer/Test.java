package com.phoenix.answer;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Scanner;

import com.phoenix.answer.worker.Worker;

public class Test {

	public static void main(String[] args) {
		
		Scanner reader = new Scanner(System.in);
		System.out.println("Enter path of your datas folder : ");
		String path = reader.next(); 
		Worker worker= new Worker(Paths.get(path));
		worker.executeAll(LocalDate.of(2017, 05, 14), 7, 100);
		reader.close(); 
	}
}
