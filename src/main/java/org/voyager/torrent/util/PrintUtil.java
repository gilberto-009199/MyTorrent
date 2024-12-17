package org.voyager.torrent.util;

import java.nio.ByteBuffer;
import java.util.*;

public class PrintUtil {
    public static void printTreeUsingCollections(Map<ByteBuffer, Object> map) {
        // Pilha para simular recursão e gerenciar níveis
        Deque<Object[]> stack = new ArrayDeque<>();
        stack.push(new Object[]{map, 0}); // Adiciona o mapa inicial com nível 0

        while (!stack.isEmpty()) {
            Object[] current = stack.pop();
            Object element = current[0];
            int depth = (int) current[1];

            String indent = String.join("", Collections.nCopies(depth * 4, " "));// Indentação baseada na profundidade

            if (element instanceof Map) {
                Map<?, ?> currentMap = (Map<?, ?>) element;
                for (Map.Entry<?, ?> entry : currentMap.entrySet()) {
                    String key = new String(((ByteBuffer) entry.getKey()).array());
                    System.out.println(indent + "- " + key + ":");

                    Object value = entry.getValue();
                    if (value instanceof Map || value instanceof List) {
                        stack.push(new Object[]{value, depth + 1}); // Adiciona à pilha
                    } else {
                        System.out.println(indent + "    " + value);
                    }
                }
            } else if (element instanceof List) {
                List<?> currentList = (List<?>) element;
                for (Object item : currentList) {
                    if (item instanceof Map || item instanceof List) {
                        stack.push(new Object[]{item, depth + 1}); // Adiciona à pilha
                    } else {
                        System.out.println(indent + "- " + item);
                    }
                }
            }
        }
    }
}
