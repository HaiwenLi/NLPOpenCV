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
    public static String FailtoUplaodFileCodeDescription = "很抱歉，因为网络阻塞，文件上传失败，请稍后再试！";
    
    // Invalid file code
    public static int InvalidFileCode = 0x0F1;
    public static String InvalidFileCodeDescription = "无效的简历文件。请重新上传PDF版本简历！";
    
    // Non Chinese file code
    public static int NonChineseFileCode = 0x0F2;
    public static String NonChineseFileCodeDescription = "目前只支持中文简历。你上传的简历中文内容太少，请重新上传！";
    
    // Fail to generate HTML code
    public static int FailtoGenerateHTMLCode = 0x0F3;
    public static String FailtoGenerateHTMLCodeDescription = "很抱歉，你的简历是神来之作，我们暂时无法处理。程序员哥哥会拼命工作，争取尽快理解你的简历～";
}
