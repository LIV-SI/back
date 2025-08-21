package com.example.talkingcharacter.mascot.controller;

import com.example.talkingcharacter.mascot.model.SceneData;
import com.example.talkingcharacter.mascot.service.VideoGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VideoController {
    @Autowired
    private VideoGenerationService videoGenerationService;

    @PostMapping("/generate-video")
    public String generateMultiSceneVideo(
            @RequestParam String sigunguEnglish,
            @RequestParam String voicepack,
            @RequestBody List<SceneData> scenes) throws Exception { // RequestBody로 SceneData 리스트를 받음

        return videoGenerationService.generateMultiSceneVideo(sigunguEnglish, voicepack, scenes);
    }
}