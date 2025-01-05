package org.voyager.torrent.client.util;

import java.io.RandomAccessFile;
import java.security.MessageDigest;

public class FileTorrentUtil {

	/**
	 * Verifica a integridade de uma peça de arquivo com base no índice e no hash SHA-1 esperado.
	 *
	 * @param index             Índice da peça no arquivo.
	 * @param expectedHash      Hash SHA-1 esperado para a peça.
	 * @param randomAccessFile  Arquivo de acesso aleatório contendo os dados.
	 * @param pieceLength       Tamanho padrão de cada peça.
	 * @param fileLength        Tamanho total do arquivo.
	 * @return true se a peça for válida, false caso contrário.
	 */
	public static boolean verify(int index,
								 byte[] expectedHash,
								 RandomAccessFile randomAccessFile,
								 int pieceLength,
								 long fileLength) {
		try {
			// Calcula o deslocamento no arquivo para a peça
			long offset = (long) index * pieceLength;

			// Calcula o número de bytes a serem lidos (última peça pode ser menor)
			int bytesToRead = (int) Math.min(pieceLength, fileLength - offset);

			// Lê os dados da peça
			byte[] buffer = new byte[bytesToRead];
			randomAccessFile.seek(offset); // Move o ponteiro para o início da peça
			randomAccessFile.readFully(buffer);

			// Calcula o hash SHA-1 dos dados lidos
			byte[] calculatedHash = calculateSHA1(buffer);

			// Compara o hash calculado com o hash esperado
			return MessageDigest.isEqual(calculatedHash, expectedHash);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Calcula o hash SHA-1 de um array de bytes.
	 *
	 * @param data Dados para os quais o hash será calculado.
	 * @return Hash SHA-1 como array de bytes.
	 */
	private static byte[] calculateSHA1(byte[] data) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			return sha1.digest(data);
		} catch (Exception e) {
			throw new RuntimeException("Erro ao calcular o hash SHA-1.", e);
		}
	}
}
