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
        getPicFromVideo(ffp, vp, 5, "E:\\picsave", "pic","E:\\picsave");
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
            int value = p.waitFor();
            System.out.println(value);
            p.destroy();
            //从视频信息中解析时长
            String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";
            Pattern pattern = Pattern.compile(regexDuration);
            Matcher m = pattern.matcher(results);
            if (m.find()) {
                int time = getTimelen(m.group(1));
                System.out.println(video_path+",视频时长："+time+", 开始时间："+m.group(2)+",比特率："+m.group(3)+"kb/s");
                return time;
            }

            /**
             * 视频在线播放
             * 如果MP4不能在线播放，则将metadata放在mp4开头，命令如下
             * ffmpeg -i D:\home\upload\36\aaa.mp4 -acodec copy -vcodec copy -movflags faststart D:\home\upload\36\bbb.mp4
             * video -i D:\home\upload\36\aaa.mp4 -acodec copy -vcodec copy -movflags faststart D:\home\upload\36\bbb.mp4
             */
            commands.add("-acodec copy -vcodec copy -moveflags faststart");
            commands.add(video_path);
            ProcessBuilder builders = new ProcessBuilder();
            builders.command(commands);
            final Process process = builders.start();
            //从输入流中读取视频信息
            int values = process.waitFor();
            System.out.println(values);
            process.destroy();
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
     * @param filePath 文件存放路径
     * @return
     */
    public static void getPicFromVideo(String ffmpeg_path, String video_path, int number, String picLocation, String picName ,String filePath) throws Exception {
        int videoLength = getVideoTime(ffmpeg_path, video_path);
        if(videoLength == 0){
            throw new Exception("获取视频长度出错！");
        }
        int start = videoLength/50;
        //以下是视频截取帧
//        for(int i=0; i < number; i++){
//            int now = start + videoLength/number*i;
//            String s = ffmpeg_path + " -i " + video_path + " -ss " + now +" -r 1 -t 1 -f image2 " + picLocation+ File.separator + picName + now + ".jpeg";
        //linux下命令
        //Runtime.getRuntime().exec(s);
        // windows下命令
//            String[] cmd = {"cmd", "/C", s};
//        }
        //以下是在线播放
        //完成后 进程必须销毁
        int now = start + videoLength/number*5;
        String cmd = ffmpeg_path + "-i" + video_path + "-ss" + now + "-r l -t l -f image2" + filePath;
        Process process = Runtime.getRuntime().exec(cmd);
        int value = process.waitFor();
        process.destroy();
    }
}
