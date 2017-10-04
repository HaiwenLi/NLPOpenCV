package Util;

//http://blog.csdn.net/caiandyong/article/details/44245647
import java.text.NumberFormat;    
import java.util.Locale;  

public class TextSimilarity {     
	 /* 
	  * �����ַ���B���ַ���A(ģ��)�����ƶ� 
	  */  
	 public static double SimilarDegree(String strA, String strB){
	 	if (strB == null || strB.isEmpty()){
	 		return 0;
	 	}
	 	
	 	String commonString;
	 	if (strA.length() > strB.length()){
            commonString = longestCommonSubstring(strA, strB);
        } else{
            commonString = longestCommonSubstring(strB, strA);
        }
	    return 1.0*commonString.length() / strB.length();          
	 }    
	 
	 /* 
	  * ���ַ�����������������д��һ�� 
	  */  
	 public static String removeSign(String str) {     
	     StringBuffer sb = new StringBuffer();   
	     //��ȡ�������ֻ���ĸ
	     for (char item : str.toCharArray())     
	         if (charReg(item)){      
	             sb.append(item);    
	         }    
	     return sb.toString();    
	 }    
	 
	 /* 
	  * �ж��ַ��Ƿ�Ϊ���֣����ֺ���ĸ�� 
	  * ��Ϊ�Է��Ž������ƶȱȽ�û��ʵ�����壬�ʷ��Ų����뿼�Ƿ�Χ�� 
	  */  
	 public static boolean charReg(char charValue) {      
	     return (charValue >= 0x4E00 && charValue <= 0X9FA5) || (charValue >= 'a' && charValue <= 'z')  
	             || (charValue >= 'A' && charValue <= 'Z')  || (charValue >= '0' && charValue <= '9');      
	 }      
	 
	   
	 /* 
	  * �󹫹��Ӵ������ö�̬�滮�㷨�� 
	  * �䲻Ҫ������õ��ַ����������ַ������������ġ� 
	  *  
	  */  
	 public static String longestCommonSubstring(String strA, String strB) {     
	     char[] chars_strA = strA.toCharArray();  
	     char[] chars_strB = strB.toCharArray();   
	     int m = chars_strA.length;     
	     int n = chars_strB.length;   
	      
	     /* 
	      * ��ʼ����������,matrix[0][0]��ֵΪ0�� 
	      * ����ַ�����chars_strA��chars_strB�Ķ�Ӧλ��ͬ����matrix[i][j]��ֵΪ���Ͻǵ�ֵ��1�� 
	      * ����matrix[i][j]��ֵ�������Ϸ��������λ�õĽϴ�ֵ�� 
	      * ��������������ֵΪ0. 
	     */  
	     int[][] matrix = new int[m + 1][n + 1];     
	     for (int i = 1; i <= m; i++) {    
	         for (int j = 1; j <= n; j++) {      
	             if (chars_strA[i - 1] == chars_strB[j - 1])     
	                 matrix[i][j] = matrix[i - 1][j - 1] + 1;      
	             else     
	                 matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);     
	         }     
	     }
	     /* 
	      * �����У����matrix[m][n]��ֵ������matrix[m-1][n]��ֵҲ������matrix[m][n-1]��ֵ�� 
	      * ��matrix[m][n]��Ӧ���ַ�Ϊ�����ַ�Ԫ�����������result�����С� 
	      *  
	      */  
	     char[] result = new char[matrix[m][n]];      
	     int currentIndex = result.length - 1;     
	     while (matrix[m][n] != 0) {     
	         if (matrix[n] == matrix[n - 1])    
	             n--;     
	         else if (matrix[m][n] == matrix[m - 1][n])      
	             m--;     
	         else {     
	             result[currentIndex] = chars_strA[m - 1];     
	             currentIndex--;    
	             n--;     
	             m--;    
	         }    
	     }      
	    return new String(result);     
	 }    
	   
	 /* 
	  * ���ת���ɰٷֱ���ʽ  
	  */     
	 public static String similarityResult(double resule){      
	     return  NumberFormat.getPercentInstance(new Locale( "en ", "US ")).format(resule);     
	 }
}

