import org.w3c.dom.ls.LSOutput;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.highgui.HighGui;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Main {
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    public BufferedImage image, decoded_image;
    public int width, height, count = 0;
    public int[][][] YCbCR, ZigZag,decoded_matrix;
    public int[][][][] squares, kvant_squares, decod_kvant, Decoded_squares;
    public double[][][][] dkp_squares;
    int[][][][] YCbCrtoRGB_res;
    public int[][] Kvant;
    public double[] coef = new double[8];
    public int[] ZG1, ZG2, ZG3;
    public String[] blokiY, blokiCb, blokiCr;
    public StringBuilder Y_ascii, Cb_ascii, Cr_ascii;
    public ArrayList<Map<Integer, Huffman.Node>> YTables, CbTables, CrTables;
    public ArrayList<String> YCodedStr, CbCodedStr, CrCodedStr;
    public StringBuilder YCodedH, CbCodedH, CrCodedH, ResultCodeH, YCodedH_ToDecode, CbCodedH_ToDecode, CrCodedH_ToDecode;
    public int[][][] bloki;
    public Mat img,decod_img;

    public Main() throws IOException {
        File file = new File("input4.png");
        image = ImageIO.read(file);
        decoded_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        decoded_image.setRGB(0,0,12);
        width = image.getWidth();
        height = image.getHeight();
        bloki = new int[(width * height / 64)][3][64];
        YCbCR = new int[width][height][3];
        squares = new int[(width * height / 64)][8][8][3];//квадраты 8x8 с 3мя составляющими Y Cb Сr
        decod_kvant = new int[(width * height / 64)][8][8][3];
        dkp_squares = new double[(width * height / 64)][8][8][3]; //квадраты 8x8 с 3мя составляющими Y Cb Сr
        kvant_squares = new int[(width * height / 64)][8][8][3]; //квадраты 8x8 с 3мя составляющими Y Cb Сr
        Decoded_squares = new int[(width * width / 64)][8][8][3];
        YCbCrtoRGB_res = new int[(width * width / 64)][8][8][3];
        blokiY = new String[width * height / 64];
        blokiCb = new String[width * height / 64];
        blokiCr = new String[width * height / 64];
        Y_ascii = new StringBuilder();
        Cb_ascii = new StringBuilder();
        Cr_ascii = new StringBuilder();
        YTables = new ArrayList<>();
        CbTables = new ArrayList<>();
        CrTables = new ArrayList<>();
        YCodedStr = new ArrayList<>();
        CbCodedStr = new ArrayList<>();
        CrCodedStr = new ArrayList<>();
        YCodedH = new StringBuilder();
        CbCodedH = new StringBuilder();
        CrCodedH = new StringBuilder();
        ResultCodeH = new StringBuilder();
        YCodedH_ToDecode = new StringBuilder();
        CbCodedH_ToDecode = new StringBuilder();
        CrCodedH_ToDecode = new StringBuilder();
        ZigZag = new int[width * height / 64][3][65];
        decoded_matrix = new int[width][height][3];
    }

    //Вспомогательные методы
    //Инициализируем коэффициенты для ДКП
    public void InitCoef() {
        coef[0] = Math.sqrt(0.125);
        for (int count = 1; count < 8; count++) {
            coef[count] = Math.sqrt(0.25);
        }
    }

    public void Matrix_Completing() {
        while (YCodedH.length() % 8 > 0)
            YCodedH.append("0");
        while (CbCodedH.length() % 8 > 0)
            CbCodedH.append("0");
        while (CrCodedH.length() % 8 > 0)
            CrCodedH.append("0");
    }

    public void Kvant_mat_creating() {
        int q = 2;
        Kvant = new int[8][8];
        System.out.println("Матрица квантования:");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Kvant[i][j] = 1 + ((1 + i + j) * q);
                System.out.print(Kvant[i][j] + " ");
            }
            System.out.println("");
        }
    }

    public static String AsciiToBinary(String asciiString) {
        int d[] = new int[asciiString.length()];
        for (int i = 0; i < asciiString.length(); i++) {
            d[i] = Integer.valueOf(asciiString.charAt(i));
        }
        StringBuilder zeros = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < asciiString.length(); i++) {
            int count = 0;
            while (count < 8 - Integer.toBinaryString(d[i]).length()) {
                zeros.append('0');
                count++;
            }
            sb.append(zeros);
            sb.append("" + Integer.toBinaryString(d[i]));
            zeros = new StringBuilder();
        }
        return sb.toString();
    }

    //
    public void ImageShow() throws UnsupportedEncodingException {
        img = Imgcodecs.imread("C:\\Users\\aiwar\\IdeaProjects\\LR4_last2\\input4.png");
        decod_img=img.clone();
        HighGui.imshow("Исходное",img);
        HighGui.waitKey();
       /* JLabel picLabel = new JLabel(new ImageIcon(image));
        JPanel jPanel1 = new JPanel();
        jPanel1.add(picLabel);
        JFrame f = new JFrame("Исходное");
        f.setSize(new Dimension(image.getWidth(), image.getHeight()));
        f.add(jPanel1);
        f.setVisible(true);*/
    }

    public void main_operations() throws UnsupportedEncodingException {
        InitCoef();
        Kvant_mat_creating();
        String Y_UTF8, Cb_UTF8, Cr_UTF8;
        Rgb2YCbCr();
        ImageShow();
        Square8x8();
        for (int[][][] square : squares) {
            DKP(square, count);
            count++;
        }
        Matrix_Completing();
        Huffman hf2 = new Huffman();
        Y_UTF8 = hf2.New_coder(YCodedH.toString());
        Cb_UTF8 = hf2.New_coder(CbCodedH.toString());
        Cr_UTF8 = hf2.New_coder(CrCodedH.toString());
        ResultCodeH.append(Y_UTF8);
        ResultCodeH.append("");
        ResultCodeH.append(Cb_UTF8);
        ResultCodeH.append("");
        ResultCodeH.append(Cr_UTF8);
        try {
            BufferedWriter out1 = new BufferedWriter(new FileWriter("coded"));
            out1.write(ResultCodeH.toString());
            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("1й квадрат Y составляющая");
        for (int i = 0; i < 8; i++)
            for (int k = 0; k < 8; k++) {
                System.out.print(squares[0][i][k][0] + " ");
                if (k > 0 && k % 7 == 0)
                    System.out.println();
            }
        System.out.println("1й квадрат Cb составляющая");
        for (int i = 0; i < 8; i++)
            for (int k = 0; k < 8; k++) {
                System.out.print(squares[0][i][k][1] + " ");
                if (k > 0 && k % 7 == 0)
                    System.out.println();
            }
        System.out.println("1й квадрат Cr составляющая");
        for (int i = 0; i < 8; i++)
            for (int k = 0; k < 8; k++) {
                System.out.print(squares[0][i][k][2] + " ");
                if (k > 0 && k % 7 == 0)
                    System.out.println();
            }
        System.out.println("1й квадрат в DKP Y составляющая");
        for (int i = 0; i < 8; i++)
            for (int k = 0; k < 8; k++) {
                System.out.printf("%.2f", dkp_squares[0][i][k][0]);
                System.out.print(" ");
                if (k > 0 && k % 7 == 0)
                    System.out.println();
            }
        System.out.println("1й квадрат квантованный Y составляющая");
        for (int i = 0; i < 8; i++)
            for (int k = 0; k < 8; k++) {
                System.out.print(kvant_squares[0][i][k][0] + " ");
                if (k > 0 && k % 7 == 0)
                    System.out.println();
            }
        System.out.println("1й квадрат в виде ЗигЗаг Y составляющая");
        for (int k = 0; k < 64; k++)
            System.out.print(ZigZag[0][0][k] + " ");
        System.out.println();
        System.out.println("1й квадрат в виде ЗигЗаг Cb составляющая");
        for (int k = 0; k < 64; k++)
            System.out.print(ZigZag[0][1][k] + " ");
        System.out.println();
        System.out.println("1й квадрат в виде ЗигЗаг Cr составляющая");
        for (int k = 0; k < 64; k++)
            System.out.print(ZigZag[0][2][k] + " ");
        System.out.println();
      /*  System.out.println("Кодирование по Хаффмену первого вектора Y");
        System.out.println(YCodedStr.get(0));
        System.out.println("Таблица");
        Set<Map.Entry<Integer, Huffman.Node>> entrySet_Y;
        entrySet_Y = YTables.get(0).entrySet();
        for (Map.Entry<Integer, Huffman.Node> pair : entrySet_Y) {
            System.out.print(pair.getKey() + "-");
            System.out.print(pair.getValue().code + "; ");
        }*/
        System.out.println();
        System.out.println("Кодовая строка для Y составляющей с добавлением нулей" + "(Длина строки-" + YCodedH.length() + ")");
        //System.out.println(YCodedH);
        System.out.println("Длина символьной строки Y-" + Y_UTF8.length());
        System.out.println("Кодовая строка для Cb составляющей с добавлением нулей" + "(Длина строки-" + CbCodedH.length() + ")");
       // System.out.println(CbCodedH);
        System.out.println("Длина символьной строки Cb-" + Cb_UTF8.length());
        System.out.println("Кодовая строка для Cr составляющей с добавлением нулей" + "(Длина строки-" + CrCodedH.length() + ")");
       // System.out.println(CrCodedH);
        System.out.println("Длина символьной строки Cr-" + Cr_UTF8.length());
        //System.out.println(Y_UTF8);

        Decoding();
    }

    public void Rgb2YCbCr() {
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {

                Color pixel = new Color(image.getRGB(i, j));
                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                int y = (int) (16 + (65.738/256 * r) + (129.057/256 * g) + (25.064/256 * b));
                int cb = (int) (128 - (37.945/256 * r) - (74.494/256 * g) + (112.439/256 * b));
                int cr = (int) (128 + (112.439/256 * r) - (94.154/256 * g) - (18.285/256 * b));
                YCbCR[i][j][0] = y;
                YCbCR[i][j][1] = cb;
                YCbCR[i][j][2] = cr;
                // System.out.print (YCbCR[i][j][0]+" "+YCbCR[i][j][1]+" "+YCbCR[i][j][2]+" R ");
            }

        }
    }

    public void Square8x8() {

        int k = 0;
        for (int k1 = 0; k1 < width / 8; k1++)
            for (int k2 = 0; k2 < width / 8; k2++) {
                for (int x = 8 * k1; x < 8 + 8 * k1; x++) {
                    for (int y = 8 * k2; y < 8 + 8 * k2; y++) {
                        squares[k][x % 8][y % 8][0] = YCbCR[x][y][0];
                        squares[k][x % 8][y % 8][1] = YCbCR[x][y][1];
                        squares[k][x % 8][y % 8][2] = YCbCR[x][y][2];
                    }
                }
                k++;
            }
        System.out.println("Квадратов- " + squares.length);
    }

    public void DKP(int[][][] square, int dkp_count) {
        double[][] F1 = new double[8][8];
        double[][] F2 = new double[8][8];
        double[][] F3 = new double[8][8];
        double sum1, sum2, sum3;
        double cos1,cos2;
        for (int u = 0; u < 8; u++)
            for (int v = 0; v < 8; v++) {
                sum1 = 0.0; sum2 = 0.0; sum3 = 0.0;
                for (int i = 0; i < 8; i++)
                    for (int j = 0; j < 8; j++) {
                        cos1 = Math.cos(((2 * i + 1) / (2.0 * 8)) * u * Math.PI);
                        cos2 = Math.cos(((2 * j + 1) / (2.0 * 8)) * v * Math.PI);
                        sum1 += cos1 * cos2 * square[i][j][0] ;
                        sum2 += cos1 * cos2 * square[i][j][1] ;
                        sum3 += cos1 * cos2 * square[i][j][2] ;
                    }
                sum1*=coef[u] * coef[v];
                sum2*=coef[u] * coef[v];
                sum3*=coef[u] * coef[v];
                F1[u][v] = sum1;
                F2[u][v] = sum2;
                F3[u][v] = sum3;
                dkp_squares[dkp_count][u][v][0] = sum1;
                dkp_squares[dkp_count][u][v][1] = sum2;
                dkp_squares[dkp_count][u][v][2] = sum3;
            }

        ZG1 = Kvantovanie(F1, dkp_count, 0);
        ZG2 = Kvantovanie(F2, dkp_count, 1);
        ZG3 = Kvantovanie(F3, dkp_count, 2);
        Huffman hf = new Huffman();
        YTables.add(hf.run(ZG1));
        YCodedH.append(hf.result);
        YCodedStr.add(hf.result.toString());
        CbTables.add(hf.run(ZG2));
        CbCodedH.append(hf.result);
        CbCodedStr.add(hf.result.toString());
        CrTables.add(hf.run(ZG3));
        CrCodedH.append(hf.result);
        CrCodedStr.add(hf.result.toString());

    }

    public int[] Kvantovanie(double[][] dkp_matrix, int square_kvant, int channel) {
        int[][] result_kv = new int[8][8];
        for (int u = 0; u < 8; u++)
            for (int v = 0; v < 8; v++) {
                result_kv[u][v] = (int) dkp_matrix[u][v] / Kvant[u][v];
                kvant_squares[square_kvant][u][v][channel] = result_kv[u][v];
            }

        return zigzag(result_kv, square_kvant, channel);
    }

    public int[] zigzag(int[][] kvant, int square_zigzag, int channel) {
        int[] result_zigzag = new int[65];
        int n = 8;
        // int[][] matrix = new int[n][n];
        int curr = 0;
        for (int diff = 1 - n; diff <= n - 1; diff++) {
            for (int i = 0; i < n; i++) {
                int j = i - diff;

                if (j < 0 || j >= n)
                    continue;
                if (((diff + n + 1) & 1) > 0) {
                    result_zigzag[curr] = kvant[i][n - 1 - j];
                } else {
                    result_zigzag[curr] = kvant[n - 1 - j][i];
                }
                ZigZag[square_zigzag][channel][curr] = result_zigzag[curr];
                curr++;
            }
        }
        result_zigzag[64] = 1111;
        return result_zigzag;
    }

    public void Decoding() throws UnsupportedEncodingException {
        int count_component = 0, count_block = 0, element_count = 0;
        StringBuilder temp_code = new StringBuilder();
        for (int i = 0; i < ResultCodeH.length(); i++) {
            if (ResultCodeH.toString().toCharArray()[i] != '') {
                if (count_component == 0) YCodedH_ToDecode.append(ResultCodeH.toString().toCharArray()[i]);
                else if (count_component == 1) CbCodedH_ToDecode.append(ResultCodeH.toString().toCharArray()[i]);
                else if (count_component == 2) CrCodedH_ToDecode.append(ResultCodeH.toString().toCharArray()[i]);
            } else count_component++;
        }

        //Для Y Компоненты
        // StringBuilder resultD = new StringBuilder();
        System.out.println("Длина символьной строки Y в декодированнии-" + YCodedH_ToDecode.length());
        String resultD_Y = AsciiToBinary(YCodedH_ToDecode.toString());
        System.out.println("Двоичный декод Y длина: " + resultD_Y.length());
        System.out.println();
        String temp_Y = "";
        // StringBuilder decod = new StringBuilder();
        StringBuilder temp_symbols_Y = new StringBuilder();
        Set<Map.Entry<Integer, Huffman.Node>> entrySet_Y;
        for (int char_count = 0; char_count < resultD_Y.length(); char_count++) {
            temp_Y += (resultD_Y.toCharArray()[char_count]);
            entrySet_Y = YTables.get(count_block).entrySet();
            for (Map.Entry<Integer, Huffman.Node> pair : entrySet_Y) {
                if (temp_Y.equals(pair.getValue().code)) {
                    if (pair.getKey() == 1111) {
                        temp_Y = "";
                        if (count_block < width * height / 64 - 1)
                            count_block++;
                        element_count = 0;
                    } else {

                        bloki[count_block][0][element_count] = pair.getKey();
                        if (element_count < 63)
                            element_count++;
                        temp_Y = "";
                    }
                }
            }
        }
        System.out.println("Блок 1й при декодироании Y составляющая");
        for (int element_k = 0; element_k < 64; element_k++)
            System.out.print(bloki[0][0][element_k] + " ");
        System.out.println();

        //Для Сb Компоненты
        count_block = 0;
        element_count = 0;
        System.out.println("Длина символьной строки Cb в декодированнии-" + CbCodedH_ToDecode.length());
        String resultD_Cb = AsciiToBinary(CbCodedH_ToDecode.toString());
        System.out.println("Двоичный декод Cb длина: " + resultD_Cb.length());
        //  System.out.println("Двоичный декод: " + resultD_Cb);
        String temp_Cb = "";
        Set<Map.Entry<Integer, Huffman.Node>> entrySet_Cb;
        for (int char_count = 0; char_count < resultD_Cb.length(); char_count++) {
            temp_Cb += (resultD_Cb.toCharArray()[char_count]);
            entrySet_Cb = CbTables.get(count_block).entrySet();
            for (Map.Entry<Integer, Huffman.Node> pair : entrySet_Cb) {
                if (temp_Cb.equals(pair.getValue().code)) {
                    if (pair.getKey() == 1111) {
                        temp_Cb = "";
                        if (count_block < width * height / 64 - 1)
                            count_block++;
                        element_count = 0;
                    } else {

                        bloki[count_block][1][element_count] = pair.getKey();
                        if (element_count < 63)
                            element_count++;
                        temp_Cb = "";
                    }
                }
            }
        }
        System.out.println("Блок 1й при декодироании Cb составляющая");
        for (int element_k = 0; element_k < 64; element_k++)
            System.out.print(bloki[0][1][element_k] + " ");
        //Для Сr Компоненты
        count_block = 0;
        element_count = 0;
        System.out.println("Длина символьной строки Cb в декодированнии-" + CbCodedH_ToDecode.length());
        String resultD_Cr = AsciiToBinary(CrCodedH_ToDecode.toString());
        System.out.println("Двоичный декод Cr длина: " + resultD_Cr.length());
        //  System.out.println("Двоичный декод: " + resultD_Cr);
        String temp_Cr = "";
        Set<Map.Entry<Integer, Huffman.Node>> entrySet_Cr;
        for (int char_count = 0; char_count < resultD_Cr.length(); char_count++) {
            temp_Cr += (resultD_Cr.toCharArray()[char_count]);
            entrySet_Cr = CrTables.get(count_block).entrySet();
            for (Map.Entry<Integer, Huffman.Node> pair : entrySet_Cr) {
                if (temp_Cr.equals(pair.getValue().code)) {
                    if (pair.getKey() == 1111) {
                        temp_Cr = "";
                        if (count_block < width * height / 64 - 1)
                            count_block++;
                        element_count = 0;
                    } else {

                        bloki[count_block][2][element_count] = pair.getKey();
                        if (element_count < 63)
                            element_count++;
                        temp_Cr = "";
                    }
                }
            }
        }
        System.out.println("Блок 1й при декодироании Cr составляющая");
        for (int element_k = 0; element_k < 64; element_k++)
            System.out.print(bloki[0][2][element_k] + " ");
        System.out.println();
        Reverse_ZigZag();
    }

    public void Reverse_ZigZag() {

        int n = 8, curr;
        int[] zg;
        for (int count_component = 0; count_component < 3; count_component++)
            for (int block_count = 0; block_count < width * height / 64; block_count++) {
                zg = bloki[block_count][count_component];
                curr = 0;
                for (int diff = 1 - n; diff <= n - 1; diff++) {
                    for (int i = 0; i < n; i++) {
                        int j = i - diff;

                        if (j < 0 || j >= n)
                            continue;
                        if (((diff + n + 1) & 1) > 0) {
                            decod_kvant[block_count][i][n - 1 - j][count_component] = zg[curr];
                        } else {
                            decod_kvant[block_count][n - 1 - j][i][count_component] = zg[curr];
                        }
                        curr++;
                    }
                }
            }
        System.out.println("Обратный ЗигЗаг 1й блок Y");
        for (int k = 0; k < 8; k++) {
            for (int m = 0; m < 8; m++)
                System.out.print(decod_kvant[0][k][m][0] + " ");
            System.out.println("");
        }
        Dekvant();
    }

    public void Dekvant() {
        for (int count_component = 0; count_component < 3; count_component++)
            for (int block_count = 0; block_count < width * height / 64; block_count++)
                for (int u = 0; u < 8; u++)
                    for (int v = 0; v < 8; v++)
                        decod_kvant[block_count][u][v][count_component] = decod_kvant[block_count][u][v][count_component] * Kvant[u][v];

        System.out.println("Деквантование первый блок Y");
        for (int k = 0; k < 8; k++) {
            for (int m = 0; m < 8; m++)
                System.out.print(decod_kvant[0][k][m][0] + " ");
            System.out.println("");
        }
        DeDKP();
    }

    public void DeDKP() {

        double cos1,cos2;
        for (int block_count = 0; block_count < width * height / 64; block_count++)
            for (int x = 0; x < 8; x++)
                for (int y = 0; y < 8; y++) {
                    int sum1 = 0, sum2 = 0, sum3 = 0;
                    for (int i = 0; i < 8; i++)
                        for (int j = 0; j < 8; j++) {
                            cos1 = Math.cos(((2 * x + 1) / (2.0 * 8)) * i * Math.PI);
                            cos2 = Math.cos(((2 * y + 1) / (2.0 * 8)) * j * Math.PI);
                            sum1 += (int) cos1 * cos2 * decod_kvant[block_count][i][j][0] * coef[i] * coef[j];
                            sum2 += (int) cos1 * cos2 * decod_kvant[block_count][i][j][1] * coef[i] * coef[j];
                            sum3 += (int) cos1 * cos2 * decod_kvant[block_count][i][j][2] * coef[i] * coef[j];
                        }
                    Decoded_squares[block_count][x][y][0] = sum1;
                    Decoded_squares[block_count][x][y][1] = sum2;
                    Decoded_squares[block_count][x][y][2] = sum3;
                }
        System.out.println("ДеДКП первый блок Y");
        for (int k = 0; k < 8; k++) {
            for (int m = 0; m < 8; m++)
                System.out.print(Decoded_squares[0][k][m][0] + " ");
            System.out.println("");
        }
        System.out.println("ДеДКП первый блок Сb");
        for (int k = 0; k < 8; k++) {
            for (int m = 0; m < 8; m++)
                System.out.print(Decoded_squares[0][k][m][1] + " ");
            System.out.println("");
        }
        System.out.println("ДеДКП первый блок Cr");
        for (int k = 0; k < 8; k++) {
            for (int m = 0; m < 8; m++)
                System.out.print(Decoded_squares[0][k][m][2] + " ");
            System.out.println("");
        }
        YCbCrtoRGB();
    }

    public void YCbCrtoRGB() {


        for (int block = 0; block < Decoded_squares.length; block++)
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++) {
                    int r = (int) ((Decoded_squares[block][i][j][0]-16)*1.164 + 1.596 * Decoded_squares[block][i][j][2] - 204.288);
                    int g = (int) ((Decoded_squares[block][i][j][0]-16)*1.164  - 0.392 * (Decoded_squares[block][i][j][1] - 128) - 0.813 * (Decoded_squares[block][i][j][2] - 128));
                    int b = (int) ((Decoded_squares[block][i][j][0]-16)*1.164 + 2.017 * (Decoded_squares[block][i][j][1] - 128));
                    if (r > 255) r = 255;
                    if (g > 255) g = 255;
                    if (b > 255) b = 255;
                    if (r < 0) r = 0;
                    if (g < 0) g = 0;
                    if (b < 0) b = 0;
                    YCbCrtoRGB_res[block][i][j][0] = r;
                    YCbCrtoRGB_res[block][i][j][1] = g;
                    YCbCrtoRGB_res[block][i][j][2] = b;
                }
        System.out.println("RGB decode");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++)
                System.out.print(YCbCrtoRGB_res[0][i][j][0] + " " + YCbCrtoRGB_res[0][i][j][1] + " " + YCbCrtoRGB_res[0][i][j][2] + " R ");
            System.out.println();
        }
        int block_count=0;
        for(int line=0;line<height/8;line++)
            for(int h=0;h<height;h++)
            {
                decoded_matrix[line*8][h][0]=YCbCrtoRGB_res[block_count][0][h%8][0];
                decoded_matrix[line*8+1][h][0]=YCbCrtoRGB_res[block_count][1][h%8][0];
                decoded_matrix[line*8+2][h][0]=YCbCrtoRGB_res[block_count][2][h%8][0];
                decoded_matrix[line*8+3][h][0]=YCbCrtoRGB_res[block_count][3][h%8][0];
                decoded_matrix[line*8+4][h][0]=YCbCrtoRGB_res[block_count][4][h%8][0];
                decoded_matrix[line*8+5][h][0]=YCbCrtoRGB_res[block_count][5][h%8][0];
                decoded_matrix[line*8+6][h][0]=YCbCrtoRGB_res[block_count][6][h%8][0];
                decoded_matrix[line*8+7][h][0]=YCbCrtoRGB_res[block_count][7][h%8][0];

                decoded_matrix[line*8][h][1]=YCbCrtoRGB_res[block_count][0][h%8][1];
                decoded_matrix[line*8+1][h][1]=YCbCrtoRGB_res[block_count][1][h%8][1];
                decoded_matrix[line*8+2][h][1]=YCbCrtoRGB_res[block_count][2][h%8][1];
                decoded_matrix[line*8+3][h][1]=YCbCrtoRGB_res[block_count][3][h%8][1];
                decoded_matrix[line*8+4][h][1]=YCbCrtoRGB_res[block_count][4][h%8][1];
                decoded_matrix[line*8+5][h][1]=YCbCrtoRGB_res[block_count][5][h%8][1];
                decoded_matrix[line*8+6][h][1]=YCbCrtoRGB_res[block_count][6][h%8][1];
                decoded_matrix[line*8+7][h][1]=YCbCrtoRGB_res[block_count][7][h%8][1];

                decoded_matrix[line*8][h][2]=YCbCrtoRGB_res[block_count][0][h%8][2];
                decoded_matrix[line*8+1][h][2]=YCbCrtoRGB_res[block_count][1][h%8][2];
                decoded_matrix[line*8+2][h][2]=YCbCrtoRGB_res[block_count][2][h%8][2];
                decoded_matrix[line*8+3][h][2]=YCbCrtoRGB_res[block_count][3][h%8][2];
                decoded_matrix[line*8+4][h][2]=YCbCrtoRGB_res[block_count][4][h%8][2];
                decoded_matrix[line*8+5][h][2]=YCbCrtoRGB_res[block_count][5][h%8][2];
                decoded_matrix[line*8+6][h][2]=YCbCrtoRGB_res[block_count][6][h%8][2];
                decoded_matrix[line*8+7][h][2]=YCbCrtoRGB_res[block_count][7][h%8][2];
                //block_index++;
                if(h%8==7) block_count++;
            }
        for (int i=0;i<width;i++)
        {
            for (int j=0;j<height;j++)
            {
                decod_img.put(j,i,decoded_matrix[i][j][2],decoded_matrix[i][j][1],decoded_matrix[i][j][0]);
            }
        }
        HighGui.imshow("Декодированное",decod_img);
        HighGui.waitKey();
    }

    public static void main(String[] args) throws IOException {
        long startTIME = System.currentTimeMillis();
        new Main().main_operations();
        long finishTime = System.currentTimeMillis();
        System.out.println(finishTime - startTIME + "ms");
    }
}
