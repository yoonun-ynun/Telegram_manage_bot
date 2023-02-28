import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class convwebm {
    public void convert(File input, File output) throws Exception{
        try {
            if(output.exists())
                return;
            FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
            FFprobe ffprobe = new FFprobe("/usr/bin/ffprobe");

            FFmpegBuilder builder = new FFmpegBuilder()
                    .addInput(ffprobe.probe(input.getAbsolutePath()))
                    .overrideOutputFiles(true)
                    .addOutput(output.getAbsolutePath())
                        .setTargetSize(100_000)
                        .setFormat("webm")
                        .setVideoPixelFormat("yuv420p")
                        .disableAudio()
                        .disableSubtitle()
                        .setVideoCodec("vp9")
                        .setVideoResolution(512, 512)
                        .setVideoFrameRate(30, 1)
                        .setStartOffset(0, TimeUnit.SECONDS)
                        .setDuration(3, TimeUnit.SECONDS)
                        .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            executor.createTwoPassJob(builder).run();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
