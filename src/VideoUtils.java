import org.apache.commons.io.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LYQ on 2017/11/2 0002.
 */
public class VideoUtils {

    public static void main(String[] args) throws Exception {
        String vp = "E:\\6BEF7F6F-5D28-48FB-A430-E199E4D071A4.mp4";
        String ffp = "E:\\ffmpeg-3.4-win64-static\\bin\\ffmpeg.exe";
        //int time = getVideoTime(ffp, vp);
        //"E:\\picsave" 为图片存放位置
        getPicFromVideo(ffp, vp, 5, "E:\\picsave", "pic");
    }

    /**
     * 获取视频总时间
     * @param video_path    视频路径
     * @param ffmpeg_path   ffmpeg路径
     * @return
     */
    public static int getVideoTime(String ffmpeg_path, String video_path) {
        List<String> commands = new ArrayList<>();
        commands.add(ffmpeg_path);
        commands.add("-i");
        commands.add(video_path);
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commands);
            final Process p = builder.start();
            //从输入流中读取视频信息
            String results = IOUtils.toString(p.getErrorStream(),"utf-8");
            //从视频信息中解析时长
            String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";
            Pattern pattern = Pattern.compile(regexDuration);
            Matcher m = pattern.matcher(results);
            if (m.find()) {
                int time = getTimelen(m.group(1));
                System.out.println(video_path+",视频时长："+time+", 开始时间："+m.group(2)+",比特率："+m.group(3)+"kb/s");
                return time;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 格式:"00:00:10.68"
    private static int getTimelen(String timelen){
        int min=0;
        String strs[] = timelen.split(":");
        if (strs[0].compareTo("0") > 0) {
            min+=Integer.valueOf(strs[0])*60*60;//秒
        }
        if(strs[1].compareTo("0")>0){
            min+=Integer.valueOf(strs[1])*60;
        }
        if(strs[2].compareTo("0")>0){
            min+=Math.round(Float.valueOf(strs[2]));
        }
        return min;
    }

    /**
     * 从视频中获取图片
     * @param ffmpeg_path ffmpeg路径
     * @param video_path 视频路径
     * @param number 获取图片数量（单位秒）
     * @param picLocation 图片位置
     * @return
     */
    public static void getPicFromVideo(String ffmpeg_path, String video_path, int number, String picLocation, String picName) throws Exception {
        int videoLength = getVideoTime(ffmpeg_path, video_path);
        if(videoLength == 0){
            throw new Exception("获取视频长度出错！");
        }
        int start = videoLength/50;
        for(int i=0; i < number; i++){
            int now = start + videoLength/number*i;
            String s = ffmpeg_path + " -i " + video_path + " -ss " + now +" -r 1 -t 1 -f image2 " + picLocation+ File.separator + picName + now + ".jpeg";
            //linux下命令
            //Runtime.getRuntime().exec(s);
            // windows下命令
            String[] cmd = {"cmd", "/C", s};
            Runtime.getRuntime().exec(cmd);
        }

    }
}
