package com.example.talkingcharacter.mascot.service;

import com.example.talkingcharacter.mascot.model.SceneData; // import 추가
import com.example.talkingcharacter.mascot.model.SpeechSegment;
import com.example.talkingcharacter.mascot.model.TtsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class VideoGenerationService {

    @Autowired
    private TtsService ttsService;
    @Autowired
    private AudioAnalysisService audioAnalysisService;
    @Autowired
    private FrameGeneratorService frameGeneratorService;
    @Autowired
    private VideoMergeService videoMergeService;
    @Autowired
    private ChromakeyMergeService chromakeyMergeService;
    @Autowired
    private SubtitleService subtitleService;
    @Autowired
    private VideoConcatService videoConcatService;

    /**
     * 여러 장면을 받아 각각 영상을 생성하고 최종적으로 하나로 합치는 메소드
     */
    public String generateMultiSceneVideo(String sigunguEnglish, String voicepack, List<SceneData> scenes) throws Exception {
        List<String> generatedSceneVideoPaths = new ArrayList<>();

        // 임시 폴더 생성
        File tempDir = new File("temp_scenes");
        if (!tempDir.exists()) tempDir.mkdirs();

        for (int i = 0; i < scenes.size(); i++) {
            SceneData scene = scenes.get(i);
            System.out.println("Processing Scene " + (i + 1) + "/" + scenes.size() + ": " + scene.getText());

            // 각 장면별로 고유한 출력 파일 경로 지정
            String sceneVideoPath = new File(tempDir, "scene_" + i + ".mp4").getAbsolutePath();

            // 각 장면을 개별 영상으로 생성
            generateSingleSceneVideo(sigunguEnglish, scene.getText(), voicepack, scene.getBGVN(), sceneVideoPath);

            generatedSceneVideoPaths.add(sceneVideoPath);
        }

        // 생성된 개별 영상들을 하나로 합침
        String finalVideoPath = "Video/final_merged_video.mp4";
        videoConcatService.concatVideos(generatedSceneVideoPaths, finalVideoPath);

        // 임시 파일 및 폴더 삭제
        clearDirectory(tempDir);
        tempDir.delete();

        return "Successfully generated multi-scene video at: " + finalVideoPath;
    }

    public void generateSingleSceneVideo(String sigunguEnglish, String text, String voicepack, String BGVN, String finalVideoPath) throws Exception {
        // 1. TTS로 오디오와 대략적인 텍스트 세그먼트 생성
        TtsResult ttsResult = ttsService.generateTts(text, voicepack);
        File wavFile = ttsResult.getAudioFile();
        List<SpeechSegment> textSegments = ttsResult.getSegments();

        // 2. 오디오 파일을 정밀 분석해 실제 소리가 나는 구간만 추출
        List<SpeechSegment> audioSegments = audioAnalysisService.analyzeSpeech(wavFile);
        double totalDuration = ttsService.getAudioDurationSeconds(wavFile);

        // 3. 자막을 위한 세그먼트 정렬 (오직 텍스트와 전체 시간 기준)
        List<SpeechSegment> finalSegmentsForSubtitles = alignSegments(textSegments, totalDuration);

        // 4. 애니메이션 프레임 생성: 고유한 임시 폴더 사용
        String framesFolder = "temp_frames"; // <<< 이렇게 고정된 이름으로 변경
        frameGeneratorService.generateFrames(audioSegments, totalDuration, sigunguEnglish, framesFolder);

        // 5. 프레임 + 음성 병합 (임시 파일)
        String mergedVideoPath = "Animation/mascot_temp.mp4";
        videoMergeService.mergeFramesWithAudio(framesFolder, wavFile.getAbsolutePath(), mergedVideoPath);

        // 6. 배경 영상과 합성 (임시 파일)
        String animationVideoPath = "Video/animationWithBGV_temp.mp4";
        chromakeyMergeService.mergeAnimationWithBakcgroundVideo("BGV/" + BGVN, mergedVideoPath, animationVideoPath);

        // 7. 자막 추가: 최종 결과물 (이것이 한 장면의 완성본)
        subtitleService.addSubtitles(animationVideoPath, finalSegmentsForSubtitles, finalVideoPath);

        // 생성 과정에서 사용된 임시 파일들 삭제
        new File(mergedVideoPath).delete();
        new File(animationVideoPath).delete();
        wavFile.delete();
    }

    private List<SpeechSegment> alignSegments(List<SpeechSegment> textSegments, double totalDuration) {
        List<SpeechSegment> finalSegments = new ArrayList<>();

        // 처리할 텍스트가 없거나 영상 길이가 0이면 빈 리스트 반환
        if (textSegments.isEmpty() || totalDuration <= 0) {
            return finalSegments;
        }

        // 1. 모든 텍스트 조각의 전체 글자 수 계산
        int totalTextLength = 0;
        for (SpeechSegment seg : textSegments) {
            totalTextLength += seg.getText().length();
        }
        if (totalTextLength == 0) {
            return finalSegments; // 글자 수가 0이면 시간 분배 불가
        }

        // 2. 전체 오디오 길이를 각 텍스트 조각의 글자 수 비율에 따라 분배
        double currentTime = 0.0;
        for (int i = 0; i < textSegments.size(); i++) {
            SpeechSegment textSeg = textSegments.get(i);
            double textLength = textSeg.getText().length();

            // 현재 텍스트 조각이 차지하는 시간 비율 계산
            double proportion = (double) textLength / totalTextLength;
            double allocatedDuration = totalDuration * proportion;

            double startTime = currentTime;
            double endTime = currentTime + allocatedDuration;

            // 마지막 자막은 반올림 오차를 없애기 위해 전체 길이에 정확히 맞춤
            if (i == textSegments.size() - 1) {
                endTime = totalDuration;
            }

            finalSegments.add(new SpeechSegment(textSeg.getText(), startTime, endTime));

            // 다음 자막의 시작 시간을 현재 자막의 끝 시간으로 설정
            currentTime = endTime;
        }

        return finalSegments;
    }

    private void clearDirectory (File dir) throws IOException {
        if (dir.exists() && dir.isDirectory()) {
            Files.walk(dir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

}