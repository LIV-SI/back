package hello.videoanalyze2;

import hello.videoanalyze2.response.VideoResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShortsMakerTest {
    String inputFile = "C:/Users/inwoo/Desktop/livsi-test/채소가게.mp4";
    File videoFile = new File(inputFile);
    VideoResult videoResult = new VideoResult();
    ShortsMaker shortsMaker = new ShortsMaker();


    @BeforeEach
    void initVideoResult() {


        String[] timeStamps = {"0:00-0:03","0:42-0:44","0:34-0:38","0:08-0:12","0:17-0:22","0:23-0:28"};
        String[] contents = {"매일 먹는 집밥, 육수부터 달라야죠!", "국물용, 조림용, 볶음용... 종류별 멸치 총집합!", "심심한 입 달래줄 밥도둑 진미채와 고소한 견과류",
                "깊은 맛의 비결, 싱싱한 건어물도 한가득!","국산 다시마와 미역까지 없는 게 없네!","우리집 밥상을 책임질 명품 건어물을 찾는다면? [가게이름]으로!"};
        List<VideoResult.Scene> sceneList = new ArrayList<>();
        for(int i = 0; i < timeStamps.length; i++){
            VideoResult.Scene scene = new VideoResult.Scene();
            scene.setOriginalTimestamp(timeStamps[i]);
            scene.setContent(contents[i]);
            scene.setSceneNumber(i);
            sceneList.add(scene);
        }

        videoResult.setConcept("매일 먹는 집밥, 육수부터 달라야죠! 국물 맛의 치트키, 온갖 건어물이 다 모인 이곳!");
        videoResult.setScenes(sceneList);
    }

    @Test
    void clip_cut() throws IOException, InterruptedException {

        shortsMaker.makeShorts(videoFile, videoResult);
    }
}