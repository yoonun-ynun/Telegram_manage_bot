import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;
import java.io.IOException;

public class convwebm {
    public void convert(File input, File output) throws Exception{
            if(output.exists())
                return;
            FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
            FFprobe ffprobe = new FFprobe("/usr/bin/ffprobe");

            FFmpegProbeResult probeResult = ffprobe.probe(input.getAbsolutePath());
            FFmpegFormat format = probeResult.getFormat();
            double second = format.duration;    //초단위
            if(second>3){
                double fast = 3/second - 0.1;
                FFmpegBuilder builder = new FFmpegBuilder()
                        .addInput(probeResult)
                        .overrideOutputFiles(true)
                        .addOutput(output.getAbsolutePath())
                            .setTargetSize(100_000)
                            .setFormat("webm")
                            .setVideoPixelFormat("rgba")
                            .disableAudio()
                            .disableSubtitle()
                            .setVideoCodec("vp9")
                            .setVideoResolution(512, 512)
                            .setVideoFrameRate(30, 1)
                            .setVideoFilter("setpts=(" + fast + ")*PTS")
                            .done();

                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

                executor.createTwoPassJob(builder).run();
            }else{

                FFmpegBuilder builder = new FFmpegBuilder()
                        .addInput(ffprobe.probe(input.getAbsolutePath()))
                        .overrideOutputFiles(true)
                        .addOutput(output.getAbsolutePath())
                        .setTargetSize(100_000)
                        .setFormat("webm")
                        .setVideoPixelFormat("rgba")
                        .disableAudio()
                        .disableSubtitle()
                        .setVideoCodec("vp9")
                        .setVideoResolution(512, 512)
                        .setVideoFrameRate(30, 1)
                        .done();

                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

                executor.createTwoPassJob(builder).run();
            }
    }
}
