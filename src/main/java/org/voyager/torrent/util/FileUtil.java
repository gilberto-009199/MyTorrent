package org.voyager.torrent.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtil {

	/**
	 * Cria um diretório se ele não existir.
	 *
	 * @param path Caminho do diretório a ser criado.
	 * @return O diretório criado ou existente.
	 */
	public static File createDirectoryIfNotExist(String path) {
		File directory = new File(path);
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				throw new RuntimeException("O caminho especificado não é um diretório: " + path);
			}
		} else {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Falha ao criar o diretório: " + path);
			}
		}
		return directory;
	}

	/**
	 * Cria um arquivo vazio se ele não existir.
	 *
	 * @param size Tamanho do arquivo em bytes.
	 * @param path Caminho do arquivo a ser criado.
	 * @return O arquivo RandomAccessFile criado ou aberto.
	 */
	public static RandomAccessFile createFileEmptyIfNotExist(long size, String path) {
		File file = new File(path);

		// Verifica se é um diretório
		if (file.isDirectory()) {
			throw new RuntimeException("O caminho especificado é um diretório: " + path);
		}

		return createFileEmptyIfNotExist(size, file);
	}

	/**
	 * Cria um arquivo vazio se ele não existir.
	 * Verifica e ajusta o tamanho caso o arquivo já exista.
	 *
	 * @param size Tamanho do arquivo em bytes.
	 * @param file Objeto File representando o arquivo.
	 * @return O arquivo RandomAccessFile criado ou aberto.
	 */
	public static RandomAccessFile createFileEmptyIfNotExist(long size, File file) {
		try {
			// Cria o arquivo se não existir
			if (!file.exists()) {
				if (!file.createNewFile()) {
					throw new RuntimeException("Falha ao criar o arquivo: " + file.getPath());
				}
			}

			// Verifica e ajusta o tamanho do arquivo se necessário
			try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
				if (randomAccessFile.length() < size) {
					randomAccessFile.setLength(size);
				}
			}

			// Retorna o arquivo aberto
			return new RandomAccessFile(file, "rw");

		} catch (FileNotFoundException e) {
			throw new RuntimeException("Arquivo não encontrado: " + file.getPath(), e);
		} catch (IOException e) {
			throw new RuntimeException("Erro ao criar ou ajustar o arquivo: " + file.getPath(), e);
		}
	}
}
