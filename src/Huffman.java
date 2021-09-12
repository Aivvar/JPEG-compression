import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.*;


public class Huffman {
   public  Map<Integer, Integer> charcount;
   public  Map<Integer, Node> charNodes;
   public  StringBuilder result;
   public class Node implements Comparable<Node>{//узел
        int sum;
        String code;
        void buildCode(String code) {
            this.code = code;
        }
        public Node(int sum){
            this.sum = sum;
        }
        @Override
        public int compareTo(Node o) {
            return Integer.compare(sum, o.sum);
        }
    }
   public class InternalNode extends Node {//внутренний узел
        Node left;
        Node right;
        @Override
        void buildCode(String code) {
            super.buildCode(code);
            left.buildCode(code + "0");
            right.buildCode(code + "1");
        }
        public InternalNode (Node left, Node right) {
            super(left.sum+ right.sum);
            this.left = left;
            this.right = right;
        }
    }
   public class LeafNode extends Node {//лист
        int symbol;
        @Override
        void buildCode(String code) {
            super.buildCode(code);
          //  System.out.println(symbol + ": " + code);
        }
        private LeafNode(int symbol, int count) {
            super(count);
            this.symbol = symbol;
        }
    }
    public Map<Integer, Node> run(int[] s) {
        charcount = new HashMap<>();
        for (int i = 0; i < s.length; i++) {
            int c = s[i];
            if (charcount.containsKey(c)) {
                charcount.put(c, charcount.get(c) + 1);
            } else {
                charcount.put(c, 1);
            }
        }
     //   System.out.println(s.length);
       /* for (Map.Entry<Integer, Integer> entry : charcount.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }*/
        charNodes = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(); //создаём приоритетную очередь для узлов
        for (Map.Entry<Integer, Integer> entry : charcount.entrySet()) {
            LeafNode node = (new LeafNode(entry.getKey(), entry.getValue()));
            charNodes.put(entry.getKey(), node);
            priorityQueue.add(node);
        }
        while (priorityQueue.size() > 1) {
            Node first = priorityQueue.poll();
            Node second = priorityQueue.poll();
            InternalNode node = new InternalNode(first, second);
            priorityQueue.add(node);
        }
       // System.out.println(charcount.size());
        Node root = priorityQueue.poll();
        assert root != null;
        root.buildCode("");
        result = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            int c = s[i];
            result.append(charNodes.get(c).code);
        }
       // System.out.println(result);
   return charNodes;
    }
    public void Decoder()
    {
        String temp_decod = "";
        StringBuilder decod = new StringBuilder();
        Set<Map.Entry<Integer, Node>> entrySet = charNodes.entrySet();
        for (int char_count = 0; char_count < result.length(); char_count++) {
            temp_decod += (result.toString().toCharArray()[char_count]);
            for (Map.Entry<Integer, Node> pair : entrySet) {
                if (temp_decod.equals(pair.getValue().code)) {
                    decod.append(pair.getKey());// нашли наше значение и возвращаем  ключ
                    decod.append(",");
                    temp_decod = "";
                }
            }
        }
      //  System.out.println(decod);
        try {
            BufferedWriter out2 = new BufferedWriter(new FileWriter("decoded"));
            out2.write(decod.toString());
            out2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
public String New_coder(String result)
{
   /* while (result.length()%8>0)
       result.append("0");*/

    byte [] data= new byte[result.length()/8];

    StringBuilder kod_str=new StringBuilder();
    char nextchar;
    int bytecount=0;
    for(int count=0;count<result.length();count+=8)
    {
        data[bytecount]=(byte) Integer.parseInt(result.substring(count, count + 8), 2);
        nextchar=(char) Integer.parseInt(result.substring(count, count + 8), 2);
        kod_str.append(nextchar);
        bytecount++;
    }
    String codedH = new String(data, US_ASCII);
/*    StringBuilder res=new StringBuilder();
    for(int count=0;count<result.length();count++)
    {
     res.append(result.toCharArray()[count]);
     if(count>0&&count%8==0)
         res.append(" ");
    }
    String raw = Arrays.stream(res.toString().split(" "))
            .map(binary -> Integer.parseInt(binary, 2))
            .map(Character::toString)
            .collect(Collectors.joining()); // cut the space
    System.out.println("Второй ");
    System.out.println(raw);*/
return kod_str.toString();
}
    public static void main(String[] args) throws FileNotFoundException {
       /* long startTIME = System.currentTimeMillis();
        try {
            new Huffman().run();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        long finishTime = System.currentTimeMillis();
        System.out.println(finishTime - startTIME + "ms");*/
    }
}