import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;
import java.io.IOException;

public class convwebm {
    public void convert(File input, File output){
        try {
            FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
            FFprobe ffprobe = new FFprobe(" /usr/bin/ffprobe");

            FFmpegBuilder builder = new FFmpegBuilder()
                    .addInput(input.getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(output.getAbsolutePath())
                        .setTargetSize(250_000)
                        .setFormat("webm")
                        .disableAudio()
                        .disableSubtitle()
                        .setVideoCodec("vp9")
                        .setVideoResolution(512, 512)
                        .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            executor.createJob(builder).run();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
