package Util;

/*
 *  Benchmark states and corresponding descriptions
 *  Author: Lantao Mei
 *  Date:   2017-10-02
 */
final public class BenchmarkState {
    // Success code
    public static int SuccessCode = 0x000;
    public static String SuccessCodeDescription = "";
    
    // Fail to upload file code
    public static int FailtoUplaodFileCode = 0x0F0;
    public static String FailtoUplaodFileCodeDescription = "�ܱ�Ǹ����Ϊ�����������ļ��ϴ�ʧ�ܣ����Ժ����ԣ�";
    
    // Invalid file code
    public static int InvalidFileCode = 0x0F1;
    public static String InvalidFileCodeDescription = "��Ч�ļ����ļ����������ϴ�PDF�汾������";
    
    // Non Chinese file code
    public static int NonChineseFileCode = 0x0F2;
    public static String NonChineseFileCodeDescription = "Ŀǰֻ֧�����ļ��������ϴ��ļ�����������̫�٣��������ϴ���";
    
    // Fail to generate HTML code
    public static int FailtoGenerateHTMLCode = 0x0F3;
    public static String FailtoGenerateHTMLCodeDescription = "�ܱ�Ǹ����ļ���������֮����������ʱ�޷���������Ա����ƴ����������ȡ���������ļ�����";
}
