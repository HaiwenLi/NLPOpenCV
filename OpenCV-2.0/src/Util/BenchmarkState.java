package Util;

/*
 *  Benchmark states and corresponding descriptions
 *  Author: Lantao Mei
 *  Date:   2017-10-02
 */
final public class BenchmarkState {
    // Success code
    public static int SuccessCode = 0x000;
    public static String SuccessDescription = "";
    
    // Fail to upload file code
    public static int FailtoUplaodFileCode = 0x0F0;
    public static String FailtoUplaodFileDescription = "很抱歉，因为网络阻塞，文件上传失败，请稍后再试！";
    
    // Invalid file code
    public static int InvalidFileCode = 0x0F1;
    public static String InvalidFileDescription = "无效的简历文件。请重新上传PDF版本简历！";
    
    // Non Chinese file code
    public static int NonChineseFileCode = 0x0F2;
    public static String NonChineseFileDescription = "目前只支持中文简历。你上传的是英文简历或中文内容太少，请重新上传！";
    
    // Fail to generate HTML code
    public static int FailtoGenerateHTMLCode = 0x0F3;
    public static String FailtoGenerateHTMLDescription = "很抱歉，我们暂时无法处理你的简历。程序员哥哥会拼命工作，争取尽快理解你的简历～";
    
    // No user
    public static int NoUserCode = 0x0F4;
    public static String NoUserDescription = "很抱歉，你还没有注册，无法进行简历评测！";
    
    // Exceed max upload counts
    public static int ExceedMaxFreeUploadTimesCode = 0x0F5;
    public static String ExceedMaxFreeUploadTimesDescription = "很抱歉，你已超过免费上传简历次数！";
    
    // Fail to analyse resume
    public static int FailtoAnalyseResumeCode = 0x0F6;
    public static String FailtoAnalyseResumeDescription = "很抱歉，在分析简历时出现错误。错误代码：0x0F6，希望你能及时发邮件至992756037@qq.com，帮助我们做得更好！";
}