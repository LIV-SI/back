package hello.livsi_0820.mascot.service;

import hello.livsi_0820.mascot.model.SpeechSegment;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;

@Service
public class FrameGeneratorService {

    // 이 값을 조절하여 캐릭터가 말하는 속도(입을 여닫는 속도)를 변경. (단위: 초)
    // 숫자가 작을수록 더 빠르게, 클수록 더 느리게 움직임
    private static final double MOUTH_FLAP_INTERVAL_SECONDS = 0.2;

    // 메소드 시그니처를 다시 audioSegments를 받도록 합니다.
    public void generateFrames(List<SpeechSegment> audioSegments, double totalDuration, String sigunguEnglish, String framesFolder) throws Exception {
        double fps = 30.0;

        File frameDir = new File(framesFolder);
        if (!frameDir.exists()) {
            frameDir.mkdirs();
        }

        InputStream openImgStream = getClass().getResourceAsStream("/region/" + sigunguEnglish + "/open.png");
        InputStream closedImgStream = getClass().getResourceAsStream("/region/" + sigunguEnglish + "/close.png");

        if (openImgStream == null || closedImgStream == null) {
            throw new Exception("Character image not found for region: " + sigunguEnglish);
        }

        BufferedImage openImg = ImageIO.read(openImgStream);
        BufferedImage closedImg = ImageIO.read(closedImgStream);

        int totalFrames = (int) Math.ceil(totalDuration * fps);

        for (int i = 0; i < totalFrames; i++) {
            double currentTime = i / fps;


            // 1. 현재 시간이 '말하는 구간'에 포함되는지 확인합니다.
            boolean isSpeakingTime = audioSegments.stream()
                    .anyMatch(s -> currentTime >= s.getStartTime() && currentTime <= s.getEndTime());

            BufferedImage currentFrameImage;

            if (isSpeakingTime) {
                // 2. 말하는 구간이라면, 일정한 간격으로 입을 여닫습니다.
                // 시간 흐름을 주기로 나누어 짝수일 때 입을 열고, 홀수일 때 닫습니다.
                int flapCycle = (int) (currentTime / (MOUTH_FLAP_INTERVAL_SECONDS / 2));
                if (flapCycle % 2 == 0) {
                    currentFrameImage = openImg; // 입 열기
                } else {
                    currentFrameImage = closedImg; // 입 닫기
                }
            } else {
                // 3. 말하는 구간이 아니라면, 입을 닫습니다.
                currentFrameImage = closedImg;
            }

            String filename = String.format("%s/frame_%04d.png", framesFolder, i);
            File outputFile = new File(filename);
            ImageIO.write(currentFrameImage, "png", outputFile);
        }
    }
}