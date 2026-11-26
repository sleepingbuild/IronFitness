package com.homefit.app.util;

import android.content.Context;
import com.homefit.app.ui.common.MediaLoader;
import com.homefit.app.ui.settings.SettingsStore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationHelper {

    private static class ExerciseInfo {
        final String nameVi, nameEn, nameKo, nameJa;
        final String descVi, descEn, descKo, descJa;

        ExerciseInfo(String nameVi, String nameEn, String nameKo, String nameJa,
                     String descVi, String descEn, String descKo, String descJa) {
            this.nameVi = nameVi;
            this.nameEn = nameEn;
            this.nameKo = nameKo;
            this.nameJa = nameJa;
            this.descVi = descVi;
            this.descEn = descEn;
            this.descKo = descKo;
            this.descJa = descJa;
        }
    }

    private static final Map<String, ExerciseInfo> DICTIONARY = new HashMap<>();

    static {
        // --- ABS ---
        putDict(new String[]{"crunches", "crunch", "basic_crunches"},
                "Gập bụng cơ bản", "Basic Crunches", "기본 크런치", "ベーシッククランチ",
                "Nằm ngửa gập bụng co đầu gối, siết cơ bụng trên nâng vai rời sàn.",
                "Lie on back with knees bent, contract abs to lift shoulders off floor.",
                "등을 대고 누워 무릎을 굽히고 복근을 수축하여 상체를 일으키세요.",
                "仰向けで膝を曲げ、腹筋を収縮させて肩を床から浮かします。");

        putDict(new String[]{"knee_plank"},
                "Plank quỳ gối", "Knee Plank", "무릎 플랭크", "膝つきプランク",
                "Quỳ gối chống cẳng tay, giữ lưng thẳng và siết chặt cơ bụng.",
                "Rest on forearms and knees, keeping your body straight.",
                "전완과 무릎을 바닥에 대고 몸을 일직선으로 유지하세요.",
                "前腕と膝を床につけ、体を直線にキープします。");

        putDict(new String[]{"plank", "elbow_plank"},
                "Plank khuỷu tay", "Elbow Plank", "엘보 플랭크", "エルボープランク",
                "Chống cẳng tay và mũi chân, giữ thân người thành một đường thẳng.",
                "Hold forearm plank position with core fully engaged.",
                "전완과 발끝으로 지탱하며 코어에 힘을 주고 자세를 유지하세요.",
                "前腕とつま先で支え、体幹に力を入れてキープします。");

        putDict(new String[]{"full_arm_plank", "high_plank"},
                "Plank thẳng tay", "High Plank", "하이 플랭크", "ハイプランク",
                "Chống thẳng hai tay rộng bằng vai và mũi chân, siết cứng cơ bụng.",
                "Hold push-up top position with straight arms and tight core.",
                "팔을 뻗은 푸시업 자세에서 코어를 단단히 조이세요.",
                "腕を伸ばした姿勢で、体幹を締めキープします。");

        putDict(new String[]{"side_plank"},
                "Plank nghiêng", "Side Plank", "사이드 플랭크", "サイドプランク",
                "Nằm nghiêng chống cẳng tay, nâng hông tạo thành đường thẳng.",
                "Lie on side resting on forearm, raise hips to form a straight line.",
                "옆으로 누워 전완으로 지탱하고 골반을 들어 올리세요.",
                "横向きになり前腕で体を支え、お尻を引き上げます。");

        putDict(new String[]{"side_plank_upandown", "side_plank_hip_lifts"},
                "Plank nghiêng nâng hông", "Side Plank Hip Lifts", "사이드 플랭크 힙 리프트", "サイドプランク・ヒップリフト",
                "Giữ tư thế plank nghiêng, nâng và hạ hông nhịp nhàng liên tục.",
                "In side plank position, lower and raise your hips rhythmically.",
                "사이드 플랭크 자세에서 골반을 위아래로 천천히 움직이세요.",
                "サイドプランクの姿勢で、腰を上下に運動させます。");

        putDict(new String[]{"plank_shoulder_taps", "shoulder_tap", "shoulder_taps"},
                "Plank chạm vai", "Plank Shoulder Taps", "플랭크 숄더 탭", "プランク・ショルダータップ",
                "Chống high plank, luân phiên dùng tay chạm vai đối diện.",
                "In high plank, alternately tap each shoulder with opposite hand.",
                "하이 플랭크에서 반대쪽 어깨를 번갈아 터치하세요.",
                "ハイプランク姿勢から交互に反対の肩をタップします。");

        putDict(new String[]{"reverse_crunches", "reverse_crunch", "reverse_cruches"},
                "Gập bụng ngược", "Reverse Crunches", "리버스 크런치", "リバースクランチ",
                "Nằm ngửa, cuộn hông nâng mông và chân hướng lên trần nhà.",
                "Lie on back, curl hips up towards chest to work lower abs.",
                "등을 대고 누워 골반을 가슴 쪽으로 말아 올리세요.",
                "仰向けで骨盤を胸に向けて丸め上げます。");

        putDict(new String[]{"heel_touch", "heel_taps"},
                "Chạm gót chân", "Heel Taps", "힐 탭", "ヒールタップ",
                "Nằm ngửa co gối, nghiêng thân sang hai bên chạm gót chân.",
                "Lie back with knees bent, crunch side to side to touch heels.",
                "누워서 상체를 살짝 일으켜 양옆으로 발꿈치를 터치하세요.",
                "仰向けで上体を浮かせ、左右のかかとにタッチします。");

        putDict(new String[]{"bicycle_crunches", "bicycle_crunch"},
                "Gập bụng đạp xe", "Bicycle Crunches", "바이시클 크런치", "バイシクルクランチ",
                "Nằm ngửa xoay cùi chỏ chạm đầu gối đối diện nhịp nhàng.",
                "Rotate opposite elbow to knee in a pedaling motion.",
                "자전거 타듯 반대쪽 팔꿈치와 무릎을 번갈아 맞닿게 하세요.",
                "自転車をこぐように反対の肘と膝を交互に引き寄せます。");

        putDict(new String[]{"leg_raises", "leg_raise"},
                "Nâng hai chân", "Leg Raises", "레그 레이즈", "レッグレイズ",
                "Nằm ngửa duỗi thẳng chân, nâng hai chân lên vuông góc sàn.",
                "Lie flat and lift straight legs vertically up and down.",
                "등을 대고 누워 펼친 다리를 천천히 들어 올렸다 내리세요.",
                "仰向けで両脚を伸ばしたままゆっくり上げ下げします。");

        putDict(new String[]{"v_crunches", "v_ups", "v_up"},
                "Gập bụng chữ V", "V-Ups", "V-업", "Vアップ",
                "Nằm ngửa, nâng đồng thời tay và chân tạo hình chữ V.",
                "Simultaneously lift torso and legs to meet in a V shape.",
                "상체와 하체를 동시에 올려 V 모양을 만드세요.",
                "上半身と下半身を同時に起こしV字を作ります。");

        putDict(new String[]{"toe_touches", "toe_touch"},
                "Chạm mũi chân", "Toe Touches", "토 터치", "トゥータッチ",
                "Nằm ngửa giơ chân lên cao, vươn tay chạm mũi chân.",
                "Lift legs up vertically and reach hands to touch toes.",
                "다리를 위로 뻗고 손으로 발가락을 터치하세요.",
                "脚を高く上げ、両手でつま先にタッチします。");

        putDict(new String[]{"hollow_hold", "hollow_body_hold"},
                "Hollow Body Hold", "Hollow Body Hold", "할로우 바디 홀드", "ホロウボディホールド",
                "Nằm ngửa nâng nhẹ vai và chân khỏi sàn, siết cứng cơ bụng.",
                "Keep lower back flat on floor while holding shoulders and legs raised.",
                "어깨와 다리를 살짝 들고 허리를 바닥에 붙여 유지하세요.",
                "肩と足を床から浮かせ、腰を床に押し付けてキープします。");

        putDict(new String[]{"knee_touch"},
                "Chạm đầu gối", "Knee Touches", "니 터치", "ニータッチ",
                "Nằm gập bụng vươn tay chạm đầu gối.",
                "Crunch up and reach hands towards your knees.",
                "상체를 일으켜 양손으로 무릎을 터치하세요.",
                "上体を起こして両手で膝にタッチします。");

        putDict(new String[]{"russian_twist"},
                "Xoay người Russian Twist", "Russian Twist", "러시안 트위스트", "ロシアントウイスト",
                "Ngồi ngả lưng nhẹ, xoay thân trên sang hai bên.",
                "Lean back slightly and twist torso from side to side.",
                "상체를 뒤로 기울이고 양옆으로 몸통을 회전하세요.",
                "上体を後ろに倒し、左右に上体を捻ります。");

        putDict(new String[]{"dead_bug"},
                "Con bọ chết Dead Bug", "Dead Bug", "데드 버그", "デッドバグ",
                "Nằm ngửa, hạ luân phiên tay và chân đối diện.",
                "Lower opposite arm and leg while maintaining flat back.",
                "누워서 반대쪽 팔과 다리를 교대로 내렸다 올리세요.",
                "仰向けで対角の手足を交互にゆっくり下げて戻します。");

        putDict(new String[]{"bird_dog"},
                "Tư thế chó chim Bird Dog", "Bird Dog", "버드 독", "バードドッグ",
                "Quỳ chống tay gối, duỗi thẳng tay và chân đối diện.",
                "On hands and knees, extend opposite arm and leg parallel to floor.",
                "네발기기 자세에서 반대쪽 팔과 다리를 일직선으로 뻗으세요.",
                "四つん張りから対角の手足を水平に伸ばします。");

        putDict(new String[]{"standing_oblique_crunch", "oblique_crunch"},
                "Gập bụng chéo đứng", "Standing Oblique Crunch", "스탠딩 오블리크 크런치", "スタンディング・オブリ-ク・クランチ",
                "Đứng thẳng, nâng gối sang bên co cùi chỏ chạm gối.",
                "Stand tall and bring knee up to meet elbow on same side.",
                "서서 무릎을 옆으로 올려 같은 쪽 팔꿈치와 만나게 하세요.",
                "直立から膝を横に引き上げ、同じ側の肘に近づけます。");

        // --- ARM ---
        putDict(new String[]{"pushup", "standard_pushup"},
                "Hít đất tiêu chuẩn", "Push-ups", "푸시업", "プッシュアップ",
                "Chống tay rộng bằng vai, hạ ngực sát sàn rồi đẩy người lên.",
                "Lower chest towards ground with hands shoulder-width apart.",
                "어깨너비로 손을 짚고 가슴이 바닥에 닿을 듯 내렸다 밀어 올리세요.",
                "肩幅に手をつき、胸が床につく直前まで下ろして押し上げます。");

        putDict(new String[]{"knee_pushup", "knee_push_up"},
                "Hít đất quỳ gối", "Knee Push-ups", "무릎 푸시업", "膝つきプッシュアップ",
                "Quỳ gối chống tay, hạ người và đẩy lên nhẹ nhàng.",
                "Perform push-ups with knees resting on floor for support.",
                "무릎을 바닥에 대고 부드럽게 푸시업을 진행하세요.",
                "膝を床につけた状態でプッシュアップを行います。");

        putDict(new String[]{"wall_pushup"},
                "Hít đất chống tường", "Wall Push-ups", "월 푸시업", "ウォールプッシュアップ",
                "Đứng đối diện tường, đẩy thân người ra vào an toàn.",
                "Stand facing a wall and push body away using arms.",
                "벽을 짚고 서서 상체를 내렸다 밀어내세요.",
                "壁に手をつき、体重をかけて押し戻します。");

        putDict(new String[]{"diamond_pushup"},
                "Hít đất kim cương", "Diamond Push-ups", "다이아몬드 푸시업", "ダイヤモンドプッシュアップ",
                "Chụm hai bàn tay thành hình kim cương tập trung cơ tay sau.",
                "Place hands together forming a diamond, focus on triceps.",
                "양손을 모아 다이아몬드 모양을 만들고 푸시업하세요.",
                "両手を合わせてダイヤモンド型を作り、三頭筋に効かせます。");

        putDict(new String[]{"pike_pushup", "pike_pushp"},
                "Hít đất chữ V Pike Push-up", "Pike Push-ups", "파이크 푸시업", "パイクプッシュアップ",
                "Nâng hông cao tạo hình chữ V ngược, hạ đầu hướng xuống sàn.",
                "Form inverted V shape with hips high, lowering head towards floor.",
                "엉덩이를 높이 들고 머리를 바닥 향해 내리며 푸시업하세요.",
                "お尻を高く上げV字を作り、頭を床に向けて下げます。");

        putDict(new String[]{"dips", "tricep_dips", "bench_dips"},
                "Nhún tay sau Triceps Dips", "Tricep Dips", "트라이셉 딥스", "リバースディップス",
                "Chống tay lên ghế, co cùi chỏ hạ hông xuống rồi đẩy lên.",
                "Lower hips by bending elbows on bench, then push back up.",
                "의자를 짚고 팔꿈치를 굽혀 내렸다 밀어 올리세요.",
                "椅子に手を置き、肘を曲げて腰を下げて押し上げます。");

        putDict(new String[]{"biceps_curl", "bicep_curl"},
                "Gập tay trước Biceps Curl", "Biceps Curl", "바이셉 컬", "バイセップカール",
                "Co cùi chỏ gập cẳng tay lên siết chặt cơ tay trước.",
                "Keep elbows fixed and curl forearm up to contract biceps.",
                "팔꿈치를 고정하고 전완을 올려 이두근을 수축하세요.",
                "肘を固定して前腕を引き上げ、二頭筋を収縮させます。");

        putDict(new String[]{"incline_pushup"},
                "Hít đất dốc lên Incline", "Incline Push-ups", "인클라인 푸시업", "インクラインプッシュアップ",
                "Chống tay lên bề mặt cao (ghế/bàn), thực hiện hít đất.",
                "Place hands on an elevated surface like a bench or step.",
                "높은 플랫폼에 손을 대고 푸시업을 하세요.",
                "台に手を置いて角度をつけ、プッシュアップを行います。");

        putDict(new String[]{"decline_pushup"},
                "Hít đất dốc xuống Decline", "Decline Push-ups", "디클라인 푸시업", "デクラインプッシュアップ",
                "Đặt chân lên ghế cao, chống tay dưới sàn hít đất.",
                "Place feet on bench and hands on floor to target upper chest.",
                "발을 의자에 올리고 바닥에서 푸시업을 진행하세요.",
                "足を台に載せ、角度をつけてプッシュアップします。");

        putDict(new String[]{"wide_pushup"},
                "Hít đất tay rộng", "Wide Push-ups", "와이드 푸시업", "ワイドプッシュアップ",
                "Chống tay rộng hơn vai, tập trung tác động cơ ngực.",
                "Place hands wider than shoulders to emphasize chest muscles.",
                "손을 어깨보다 넓게 짚고 푸시업하세요.",
                "手幅を肩幅より広く取り、胸筋を意識して行います。");

        putDict(new String[]{"clap_pushup", "explosive_pushup"},
                "Hít đất vỗ tay", "Clap Push-ups", "클랩 푸시업", "クラッププッシュアップ",
                "Dùng lực đẩy thân người lên cao vỗ tay trước khi hạ.",
                "Push up explosively off floor and clap hands before landing.",
                "폭발적으로 몸을 밀어 올려 손뼉을 치고 착지하세요.",
                "爆発的に押し上げ、空中でお辞儀するように手を叩きます。");

        putDict(new String[]{"close_grip_pushup"},
                "Hít đất tay hẹp", "Close-Grip Push-ups", "클로즈 그립 푸시업", "ナロープッシュアップ",
                "Chống hai tay sát nhau trước ngực ép cơ tay sau.",
                "Place hands close together beneath chest for triceps load.",
                "양손을 가슴 아래 좁게 모으고 푸시업하세요.",
                "手幅を狭くして胸の下に置き、上腕三頭筋を鍛えます。");

        putDict(new String[]{"doorframe_bicep_pull", "bicep_pull"},
                "Gập tay trước với khung cửa", "Doorframe Bicep Pull", "문틀 바이셉 풀", "ドアフレーム・バイセッププル",
                "Nắm mép khung cửa kéo người áp sát bằng cơ tay trước.",
                "Grasp doorframe with one arm and pull chest towards frame.",
                "문틀을 잡고 이두근의 힘으로 몸을 당기세요.",
                "ドア枠を片手で掴み、二頭筋の力で引き寄せます。");

        // --- CHEST ---
        putDict(new String[]{"chest_stretch"},
                "Giãn cơ ngực", "Chest Stretch", "가슴 스트레칭", "胸のストレッチ",
                "Đan hai tay ra sau lưng, mở rộng ngực tối đa.",
                "Clasp hands behind back and pull shoulders open to stretch chest.",
                "양손을 뒤로 잡아 가슴을 펼치고 늘려주세요.",
                "手を後ろで組み、胸を大きく開いてストレッチします。");

        putDict(new String[]{"chest_squeeze", "chest_press"},
                "Ép ngực Isometric", "Isometric Chest Press", "가슴 프레스", "チェストプレス",
                "Ép hai lòng bàn tay vào nhau trước ngực gồng siết cơ ngực.",
                "Press palms together forcefully in front of chest.",
                "가슴 앞에서 손바닥을 밀착시켜 가슴에 힘을 주세요.",
                "胸の前で両手のひらを強く押し合わせます。");

        putDict(new String[]{"cobra_stretch"},
                "Giãn cơ rắn cobra", "Cobra Stretch", "코브라 스트레칭", "コブラストレッチ",
                "Nằm sấp chống tay nâng ngực ngửa đầu giãn bụng ngực.",
                "Lie face down and press hands to lift chest and arch back.",
                "엎드려 손으로 바닥을 밀며 상체를 올려 복부를 늘리세요.",
                "うつ伏せから手で床を押し、上体を起こします。");

        // --- BACK & SHOULDER ---
        putDict(new String[]{"superman_stretch", "superman"},
                "Tư thế siêu nhân Superman", "Superman Hold", "슈퍼맨 홀드", "スーパーマンホールド",
                "Nằm sấp, nâng đồng thời tay và chân rời khỏi sàn.",
                "Lie face down and lift arms and legs off floor.",
                "엎드려서 팔과 다리를 바닥에서 들어 올리세요.",
                "うつ伏せで両手両脚を同時に床から浮かせます。");

        putDict(new String[]{"superman_pulses", "superman_pulse"},
                "Nhấp tay chân Superman Pulse", "Superman Pulses", "슈퍼맨 펄스", "スーパーマンパルス",
                "Nằm sấp nhấp nâng tay chân liên tục nhịp nhàng.",
                "Pulse arms and legs up and down while maintaining prone position.",
                "슈퍼맨 자세에서 팔다리를 리드미컬하게 위아래로 움직이세요.",
                "スーパーマン姿勢で手足を細かく上下に動かします。");

        putDict(new String[]{"prone_y_raise", "y_raise"},
                "Nằm sấp giơ tay chữ Y", "Prone Y-Raise", "엎드려 Y 레이즈", "プローンYレイズ",
                "Nằm sấp dang tay chữ Y, nâng cẳng tay siết cơ vai sau.",
                "Lie prone with arms in Y position and raise thumbs up.",
                "엎드려 Y자로 팔을 뻗고 어깨 힘으로 올려주세요.",
                "うつ伏せでY字に腕を開き、上背部で引き上げます。");

        putDict(new String[]{"prone_t_raise", "t_raise"},
                "Nằm sấp giơ tay chữ T", "Prone T-Raise", "엎드려 T 레이즈", "プローンTレイズ",
                "Nằm sấp dang tay chữ T, nâng cẳng tay ép bả vai.",
                "Lie prone with arms in T position and squeeze shoulder blades.",
                "T자 모양으로 팔을 뻗어 견갑골을 모아주세요.",
                "T字に腕を広げてうつ伏せになり、肩甲骨を寄せて上げます。");

        putDict(new String[]{"good_morning", "good_mornings"},
                "Gập hông Good Mornings", "Good Mornings", "굿모닝", "グッドモーニング",
                "Đứng thẳng đặt tay sau đầu, gập hông giữ lưng thẳng.",
                "Hinge at hips keeping spine flat to stretch hamstrings and back.",
                "손을 머리 뒤에 대고 허리를 펴며 골반을 접으세요.",
                "手を頭の後ろに当て、背筋を伸ばしたまま股関節を曲げます。");

        putDict(new String[]{"wall_angels"},
                "Trượt tay dải tường Wall Angels", "Wall Angels", "월 엔젤", "ウォールエンジェル",
                "Dựa lưng vào tường, trượt hai tay lên xuống áp sát mặt tường.",
                "Slide arms up and down against a wall maintaining back contact.",
                "벽에 등을 대고 팔을 위아래로 슬라이드하세요.",
                "壁に背中をつけ、腕を上下にスライドさせます。");

        putDict(new String[]{"shoulder_stretch"},
                "Giãn cơ vai", "Shoulder Stretch", "어깨 스트레칭", "肩のストレッチ",
                "Kéo cẳng tay ngang qua ngực dùng tay kia ép nhẹ.",
                "Pull arm across chest to stretch shoulder joint.",
                "한쪽 팔을 가슴 앞으로 가져와 당겨주세요.",
                "片腕を胸の前に引き寄せ、反対の手で軽く押さえます。");

        // --- LEGS & GLUTES ---
        putDict(new String[]{"squat", "bodyweight_squat", "squats"},
                "Ngồi xổm Squats", "Squats", "스쿼트", "スクワット",
                "Đứng chân rộng bằng vai, hạ hông như ngồi ghế rồi đứng lên.",
                "Lower hips back and down with knees tracking over toes.",
                "엉덩이를 뒤로 빼며 무릎을 굽혀 하강했다 서세요.",
                "お尻を後ろに引いて膝を曲げ、下ろして立ち上がります。");

        putDict(new String[]{"jump_squat", "jump_squats"},
                "Squat bật nhảy", "Jump Squats", "점프 스쿼트", "ジャンプスクワット",
                "Hạ người xuống squat rồi bật nhảy cao lên không trung.",
                "Perform squat and jump explosively into air.",
                "스쿼트 후 폭발적으로 점프하세요.",
                "スクワットから爆発的に高くジャンプします。");

        putDict(new String[]{"sumo_squat", "sumo_squats"},
                "Squat chân rộng Sumo", "Sumo Squats", "스모 스쿼트", "スモウススクワット",
                "Đứng chân rộng hơn vai mũi chân hướng ra ngoài, hạ sâu hông.",
                "Wide stance squat with toes pointing outward.",
                "다리를 넓게 벌리고 발끝을 외측으로 둔 채 하강하세요.",
                "脚を広めに開き、つま先を外に向けて深めにしゃがみます。");

        putDict(new String[]{"bulgarian_split_squat", "split_squat"},
                "Bulgarian Split Squat", "Bulgarian Split Squat", "불가리안 스플릿 스쿼트", "ブルガリアンスプリットスクワット",
                "Đặt chân ra sau lên ghế, hạ gối chân trước xuống 90 độ.",
                "Rest rear foot on elevated surface and bend front knee.",
                "한쪽 발을 뒤 의자에 대고 앞 무릎을 굽혀 내리세요.",
                "片足を後ろの台に載せ、前脚の膝を90度まで曲げ下げます。");

        putDict(new String[]{"lunge", "reverse_lunge", "lunges"},
                "Bước chùng chân Lunges", "Lunges", "런지", "ランジ",
                "Bước chân ra trước hoặc sau, hạ hai gối góc 90 độ.",
                "Step one leg forward/back, lowering hips until both knees form 90 degrees.",
                "한 발을 내딛고 양 무릎을 90도로 굽히세요.",
                "一歩踏み出し、両膝が90度になるまで腰を下げます。");

        putDict(new String[]{"glute_bridge"},
                "Cây cầu nâng hông Glute Bridge", "Glute Bridge", "글루트 브릿지", "グルートブリッジ",
                "Nằm ngửa co gối, đẩy gót chân nâng hông cao siết mông.",
                "Lie back with knees bent and drive heels to lift hips.",
                "누워서 무릎을 굽히고 발꿈치로 골반을 끌어올리세요.",
                "仰向けで膝を曲げ、かかとで床を押してヒップを引き上げます。");

        putDict(new String[]{"single_leg_glute_bridge"},
                "Glute Bridge một chân", "Single-Leg Glute Bridge", "싱글 렉 글루트 브릿지", "シングルレッグ・グルートブリッジ",
                "Nằm ngửa giơ một chân lên cao, đẩy hông bằng chân còn lại.",
                "Extend one leg in air and drive opposite heel to lift hips.",
                "한 다리를 들고 반대쪽 발꿈치로 엉덩이를 올리세요.",
                "片脚を伸ばしたまま上げ、反対の脚で腰を押し上げます。");

        putDict(new String[]{"donkey_kick", "donkey_kicks"},
                "Đá chân sau Donkey Kicks", "Donkey Kicks", "동키 킥", "ドンキーキック",
                "Quỳ chống tay gối, đá gót chân một bên lên trần nhà siết mông.",
                "On hands and knees, kick one heel up towards ceiling.",
                "네발기기에서 발꿈치를 천장 향해 차올리세요.",
                "四つん張りから片脚のかかとを天井に向けてキックします。");

        putDict(new String[]{"calf_raises", "calf_raise"},
                "Nhón gót chân Calf Raises", "Calf Raises", "카프 레이즈", "カーフレイズ",
                "Đứng thẳng nhón gót chân lên cao tối đa rồi hạ xuống.",
                "Raise heels off floor onto tiptoes and lower slowly.",
                "발끝으로 서서 발꿈치를 올렸다 내리세요.",
                "まっすぐ立ち、つま先立ちでかかとを高く上げ下げします。");

        putDict(new String[]{"wall_sit"},
                "Ngồi dựa tường Wall Sit", "Wall Sit", "월 시트", "ウォールシット",
                "Dựa lưng vào tường hạ đùi song song mặt sàn, giữ tư thế.",
                "Press back against wall with thighs parallel to floor.",
                "벽에 등을 대고 무릎을 90도로 굽혀 유지하세요.",
                "壁に背中をつけ、太ももが床と平行になる高さでキープします。");

        putDict(new String[]{"quad_stretch", "standing_quad_stretch"},
                "Giãn cơ đùi trước", "Quad Stretch", "대퇴사두근 스트레칭", "大腿四頭筋ストレッチ",
                "Đứng kéo cổ chân sát mông duỗi căng cơ đùi trước.",
                "Pull ankle towards glutes to stretch thigh front.",
                "발목을 잡고 엉덩이에 당겨 앞허벅지를 늘리세요.",
                "足首を後ろで持ち、かかとをお尻に寄せて太もも前部を伸ばします。");

        putDict(new String[]{"hamstring_stretch", "standing_hamstring_stretch"},
                "Giãn cơ đùi sau", "Hamstring Stretch", "햄스트링 스트레칭", "ハムストリング・ストレッチ",
                "Duỗi thẳng chân gập hông kéo giãn cơ đùi sau.",
                "Extend leg and reach forward to stretch back of thigh.",
                "다리를 펴고 상체를 숙여 뒷허벅지를 늘리세요.",
                "脚を伸ばして上体を倒し、太もも裏側を伸ばします。");

        putDict(new String[]{"glute_stretch", "figure_4_stretch"},
                "Giãn cơ mông", "Glute Stretch", "둔근 스트레칭", "臀部ストレッチ",
                "Nằm ngửa bắt chéo chân hình số 4, kéo đùi về ngực.",
                "Cross ankle over knee and pull thigh in to stretch glutes.",
                "다리를 꼬아 가슴 쪽으로 당겨 엉덩이를 늘리세요.",
                "足首を反対の膝に載せ、胸に引き寄せてお尻をストレッチします。");

        putDict(new String[]{"hip_flexor_stretch", "kneeling_hip_flexor_stretch"},
                "Giãn gân háng", "Hip Flexor Stretch", "장요근 스트레칭", "腸腰筋ストレッチ",
                "Quỳ lunge một chân, đẩy hông về trước kéo giãn cơ gân háng.",
                "Kneel on one knee and push hips forward to stretch flexors.",
                "무릎을 꿇고 골반을 앞으로 밀어 장요근을 늘리세요.",
                "片膝をつき、腰を前に押し出して股関節前部を伸ばします。");

        putDict(new String[]{"step_up"},
                "Bước lên bục Step-ups", "Step-Ups", "스텝업", "ステップアップ",
                "Bước từng chân lên ghế/bục chắc chắn rồi hạ xuống.",
                "Step up onto a box/chair with one leg and drive up.",
                "의자나 플랫폼 위에 한 발씩 올라섰다 내리세요.",
                "台の上に片足ずつ乗り、上体を押し上げます。");

        // --- CARDIO ---
        putDict(new String[]{"mountain_climber", "mountain_climbers"},
                "Chạy leo núi Mountain Climbers", "Mountain Climbers", "마운틴 클라이머", "マウンテンクライマー",
                "Chống high plank, kéo gối luân phiên về phía ngực nhanh.",
                "In high plank, drive knees rapidly towards chest.",
                "하이 플랭크에서 무릎을 가슴 쪽으로 빠르게 교대로 당기세요.",
                "ハイプランクから膝を交互に胸へ素早く引き寄せます。");

        putDict(new String[]{"burpees", "burpee"},
                "Bật nhảy Burpees", "Burpees", "버피", "バーピー",
                "Squat hạ người chống plank, hít đất rồi bật nhảy cao.",
                "Squat down, jump to plank, push up, jump feet back and leap up.",
                "엎드려 푸시업 후 발을 당겨 위로 점프하세요.",
                "しゃがんで足を開きプッシュアップ、引き寄せてジャンプします。");

        putDict(new String[]{"high_knees", "high_knee"},
                "Chạy nâng cao đùi", "High Knees", "하이 니", "ハイニー",
                "Chạy tại chỗ nâng cao đầu gối ngang hông.",
                "Run in place driving knees up to hip height.",
                "제자리에서 무릎을 높이 올려 빠르게 뛰세요.",
                "その場で膝を腰の高さまで高く引き上げて走ります。");

        putDict(new String[]{"butt_kicks"},
                "Đá gót chạm mông", "Butt Kicks", "버트 킥", "バットキック",
                "Chạy tại chỗ gập gối đá gót chạm mông.",
                "Run in place kicking heels up towards glutes.",
                "제자리에서 뛰며 발꿈치가 엉덩이에 닿도록 하세요.",
                "その場で走り、かかとがお尻に触れるように蹴り上げます。");

        putDict(new String[]{"plank_jack", "plank_jacks"},
                "Plank bật nhảy Plank Jacks", "Plank Jacks", "플랭크 잭", "プランクジャック",
                "Chống high plank, bật nhảy hai chân ra vào liên tục.",
                "In high plank position, jump feet out wide and together.",
                "플랭크 자세에서 양발을 밖으로 점프했다 모으세요.",
                "プランク姿勢で足を開閉するようにジャンプします。");

        // --- WARM UP ---
        putDict(new String[]{"jumping_jack", "jumping_jacks"},
                "Jumping Jacks", "Jumping Jacks", "점핑 잭", "ジャンピングジャック",
                "Bật nhảy dang hai chân kết hợp giơ tay qua đầu.",
                "Jump spreading feet wide while bringing arms overhead.",
                "양발을 벌리며 팔을 머리 위로 올려 점프하세요.",
                "足を広げながら手を頭上に上げてジャンプします。");

        putDict(new String[]{"arm_circles", "arm_circle"},
                "Xoay tay Arm Circles", "Arm Circles", "팔 돌리기", "アームサークル",
                "Dang hai tay sang ngang xoay tròn cánh tay nhịp nhàng.",
                "Extend arms out and rotate them in smooth circles.",
                "양팔을 옆으로 펼쳐 원을 그리며 돌려주세요.",
                "腕を左右に広げ、円を描くように回します。");

        putDict(new String[]{"hip_circles", "hip_circle"},
                "Xoay hông Hip Circles", "Hip Circles", "골반 돌리기", "ヒップサークル",
                "Chống tay lên hông, xoay hông tròn theo chiều kim đồng hồ.",
                "Place hands on hips and rotate hips in large smooth circles.",
                "손을 허리에 대고 골반을 크게 돌려주세요.",
                "手を腰に当て、骨盤を大きく円を描いて回します。");

        putDict(new String[]{"leg_swings"},
                "Đu chân Leg Swings", "Leg Swings", "다리 스윙", "レッグスイング",
                "Vịn tay vào tường, đung đung chân ra trước và sau.",
                "Hold support and swing leg smoothly forward and backward.",
                "벽을 잡고 다리를 앞뒤로 흔들어 풀어주세요.",
                "壁を手で支え、脚を前後へ振り子のように振ります。");

        putDict(new String[]{"inchworm", "inchworms"},
                "Bò sâu Inchworm", "Inchworms", "인치웜", "インチワーム",
                "Gập người bò hai tay ra tư thế plank rồi bò tay lùi lại.",
                "Bend down, walk hands out into a plank, then walk back.",
                "상체를 숙여 손으로 걸어나가 플랭크를 만든 후 돌아오세요.",
                "前屈して手を床につけ、前へ歩かせてプランクを作り戻ります。");

        // --- STRETCHING ---
        putDict(new String[]{"child_pose", "childs_pose"},
                "Tư thế em bé Child's Pose", "Child's Pose", "어린이 자세", "チャイルドポーズ",
                "Quỳ gối ngồi lên gót chân, vươn thẳng tay gập người trán chạm sàn.",
                "Kneel, sit back on heels, stretch arms out front with forehead resting on ground.",
                "무릎을 꿇고 엉덩이를 발꿈치에 대고 팔을 앞으로 뻗으세요.",
                "正座から腰を落とし、腕を前に伸ばして額を床につけます。");

        putDict(new String[]{"cat_cow_stretch", "cat_cow"},
                "Tư thế Mèo Bò Cat-Cow", "Cat-Cow Stretch", "캣 카우 스트레칭", "キャット＆カウ",
                "Quỳ chống tay gối, luân phiên uốn cong và võng cột sống.",
                "On all fours, rhythmically arch and round your spine.",
                "네발기기에서 등과 허리를 번갈아 곡선으로 만드세요.",
                "四つん張りから背中を丸めたり反らせたりを繰り返します。");

        putDict(new String[]{"seated_forward_fold"},
                "Duỗi chân gập người Seated Fold", "Seated Forward Fold", "앉아 상체 숙이기", "長座体前屈",
                "Ngồi duỗi chân thẳng, vươn tay gập người về phía mũi chân.",
                "Sit with straight legs and hinge forward to stretch hamstrings.",
                "다리를 뻗고 앉아 상체를 숙여 발을 잡으세요.",
                "脚を伸ばして座り、上体を倒してつま先を目指します。");

        putDict(new String[]{"lying_spinal_twist"},
                "Nằm xoay cột sống", "Lying Spinal Twist", "누워 척추 비틀기", "仰向けワニのポーズ",
                "Nằm ngửa co gối, ép hai gối sang một bên quay đầu hướng ngược lại.",
                "Lie back, drop knees to side and gaze in opposite direction.",
                "누워서 무릎을 옆으로 넘기고 고개는 반대로 돌리세요.",
                "仰向けで膝を片側に倒し、顔を反対側に向けて捻ります。");

        putDict(new String[]{"worlds_greatest_stretch"},
                "World's Greatest Stretch", "World's Greatest Stretch", "월드 그레이티스트 스트레칭", "ワールド・グレイテスト・ストレッチ",
                "Bước lunge sâu xoay vươn tay cùng bên lên trần nhà.",
                "Step into deep lunge with hand planted and twist torso upward.",
                "깊은 런지에서 손을 짚고 몸통을 천장 향해 돌리세요.",
                "深めのランジから手を床につき、上体を天井に向けて開きます。");

        // --- FULL BODY ---
        putDict(new String[]{"star_jump", "star_jumps"},
                "Bật nhảy ngôi sao Star Jumps", "Star Jumps", "스타 점프", "スタージャンプ",
                "Bật nhảy bung xòe tay chân hình ngôi sao trên không.",
                "Explosively jump stretching arms and legs out into a star shape.",
                "공중으로 튀어 올라 팔다리를 별 모양으로 펼치세요.",
                "空中へ爆発的に跳び、手足を星型に広げます。");

        putDict(new String[]{"bear_crawl"},
                "Bò gấu Bear Crawl", "Bear Crawl", "베어 크롤", "ベアクロール",
                "Quỳ gối nâng gối khỏi sàn 5cm, bò tiến lùi giữ lưng thẳng.",
                "Crawl on hands and feet keeping knees hovering just above floor.",
                "무릎을 살짝 띄우고 손발로 네발기기로 기어가세요.",
                "膝を浮かせた状態の手足で四つん這いに移動します。");

        putDict(new String[]{"man_maker"},
                "Man Maker toàn thân", "Man Maker", "맨 메이커", "マンメーカー",
                "Kết hợp squat, plank, hít đất và bật nhảy liên hoàn.",
                "Perform squat, plank, push-up and explosive jump in sequence.",
                "스쿼트, 플랭크, 푸시업, 점프를 연속 수행하세요.",
                "スクワット、プランク、押し上げ、ジャンプを連続で行います。");

        putDict(new String[]{"windshield_wipers"},
                "Gạt nước kính Windshield Wipers", "Windshield Wipers", "윈드실드 와이퍼", "ウィンドシールドワイパー",
                "Nằm ngửa nâng chân cao, xoay chân sang hai bên như cần gạt nước.",
                "Lie on back with legs raised and rotate side to side.",
                "누워서 다리를 들고 와이퍼처럼 양옆으로 넘기세요.",
                "仰向けで脚を上げ、ワイパーのように左右に倒します。");

        putDict(new String[]{"dragon_flag"},
                "Dragon Flag", "Dragon Flag", "드래곤 플래그", "ドラゴンフラッグ",
                "Tựa lưng vai nâng toàn bộ thân người lên thẳng rồi hạ chậm.",
                "Lift body up supported on shoulders and lower slowly.",
                "어깨로 지탱하며 몸 전체를 들어 올렸다 내리세요.",
                "肩で支えて体全体を浮かせ、ゆっくり下ろします。");

        putDict(new String[]{"ab_wheel_rollout"},
                "Ab Wheel Rollout", "Ab Wheel Rollout", "앱 휠 롤아웃", "アブローラー・ロールアウト",
                "Quỳ gối lăn con lăn cơ bụng ra xa rồi kéo về.",
                "Roll wheel forward from knees extending body straight.",
                "무릎을 꿇고 휠을 앞으로 굴려 몸을 뻗으세요.",
                "膝立ちからローラーを前に転がし体を伸ばします。");

        putDict(new String[]{"l_sit", "v_sit"},
                "L-Sit Hold", "L-Sit Hold", "L-시트 홀드", "Lシットホールド",
                "Chống tay nâng toàn bộ cơ thể duỗi chân vuông góc.",
                "Support body on hands with legs extended parallel to floor.",
                "손으로 바닥을 짚고 다리를 뻗어 바닥과 평행하게 유지하세요.",
                "手で体を支え、脚を伸ばして床と平行に浮かせます。");

        putDict(new String[]{"hanging_leg_raise"},
                "Hanging Leg Raise", "Hanging Leg Raise", "행잉 레그 레이즈", "ハンギング・レッグレイズ",
                "Đu xà đơn nâng hai chân lên song song với sàn.",
                "Hang from bar and raise straight legs to waist height.",
                "철봉에 매달려 다리를 직각으로 올려 복근을 자극하세요.",
                "バーにぶら下がり、脚を腰の高さまで引き上げます。");

        putDict(new String[]{"archer_pushup"},
                "Hít đất bắn cung Archer Push-up", "Archer Push-ups", "아처 푸시업", "アーチャープッシュアップ",
                "Dồn lực hạ người về một bên tay, tay kia duỗi thẳng.",
                "Lower body towards one arm while extending opposite arm out.",
                "한쪽 팔로 체중을 실으며 반대쪽 팔을 펼치세요.",
                "片腕に体重を乗せ、反対の腕を伸ばして下ろします。");

        putDict(new String[]{"staggered_pushup"},
                "Hít đất tay lệch Staggered Push-up", "Staggered Push-ups", "스태거드 푸시업", "スタッガード・プッシュアップ",
                "Đặt một tay cao một tay thấp thực hiện hít đất.",
                "Perform push-ups with hands offset one higher than the other.",
                "양손의 위치를 앞뒤로 어긋나게 짚고 푸시업하세요.",
                "手の手幅を前後にして角度をつけ、プッシュアップします。");

        putDict(new String[]{"handstand_pushup"},
                "Hít đất trồng cây chuối Handstand", "Handstand Push-ups", "핸드스탠드 푸시업", "ハンドスタンド・プッシュアップ",
                "Trồng cây chuối dựa tường hạ đầu đẩy người lên.",
                "Invert against wall and lower head towards ground.",
                "벽에 물구나무서서 머리를 바닥으로 내렸다 올리세요.",
                "壁倒立から頭を床に向けて下げ、押し上げます。");

        putDict(new String[]{"pistol_squat"},
                "Squat một chân Pistol Squat", "Pistol Squats", "피스톨 스쿼트", "ピストルスクワット",
                "Duỗi một chân ra trước, hạ người squat bằng một chân.",
                "Single leg squat with non-working leg extended out front.",
                "한 다리를 앞으로 뻗은 채 외다리로 스쿼트하세요.",
                "片脚を前に伸ばしたまま、1本脚で深めにしゃがみます。");

        putDict(new String[]{"cossack_squat"},
                "Cossack Squat", "Cossack Squat", "코삭 스쿼트", "コサックスクワット",
                "Dồn trọng tâm hạ sâu người sang một bên chân.",
                "Shift weight to one side in deep side lunge.",
                "한쪽 다리로 체중을 실어 깊게 가라앉으세요.",
                "片側に体重をかけて深くしゃがみ込みます。");

        putDict(new String[]{"walking_lunge"},
                "Lunge bước đi Walking Lunge", "Walking Lunges", "워킹 런지", "ウォーキングランジ",
                "Bước tiến về phía trước liên tục thực hiện lunge.",
                "Lunge forward continuously stepping foot over foot.",
                "앞으로 걸어나가며 연속으로 런지를 수행하세요.",
                "前進しながら連続でランジを行います。");

        putDict(new String[]{"hip_thrust"},
                "Hip Thrust nâng hông", "Hip Thrust", "힙 쓰러스트", "ヒップスラスト",
                "Tựa vai lên ghế, đẩy hông cao siết chặt cơ mông.",
                "Rest upper back on bench and drive hips upward.",
                "상체를 벤치에 대고 골반을 강력하게 차올리세요.",
                "背中をベンチに預け、腰を高く押し上げます。");

        putDict(new String[]{"shrimp_squat"},
                "Shrimp Squat", "Shrimp Squat", "쉬림프 스쿼트", "シュリンプスクワット",
                "Tay giữ cổ chân sau, hạ người squat bằng chân trước.",
                "Hold foot behind back and squat down on front leg.",
                "뒤 발목을 잡고 한 다리로 하강하세요.",
                "後ろの足首を持ち、片脚でコントロールしながらしゃがみます。");

        putDict(new String[]{"nordic_curl"},
                "Nordic Curl đùi sau", "Nordic Hamstring Curl", "노르딕 햄스트링 컬", "ノーディックハムストリングカール",
                "Cố định cổ chân, ngả thân người về trước bằng cơ đùi sau.",
                "Anchor ankles and lower torso forward using hamstrings.",
                "발목을 고정하고 햄스트링 힘으로 몸을 내리세요.",
                "足首を固定し、太もも裏の力で体をゆっくり倒します。");

        putDict(new String[]{"sissy_squat"},
                "Sissy Squat", "Sissy Squat", "씨씨 스쿼트", "シシースクワット",
                "Đẩy đầu gối ra trước, ngả thân người ra sau cô lập đùi trước.",
                "Lean torso back while bending knees forward to isolate quads.",
                "무릎을 앞으로 내밀며 상체를 뒤로 젖혀 대퇴사두근을 자극하세요.",
                "膝を前に出し上体を後ろに倒し、太もも前部を孤立させます。");

        putDict(new String[]{"inchworm_to_pushup"},
                "Inchworm kết hợp Push-up", "Inchworm to Push-up", "인치웜 푸시업", "インチワーム・プッシュアップ",
                "Bò tay ra plank, thực hiện hít đất rồi bò tay lùi lại.",
                "Walk hands out to plank, perform push-up, walk hands back.",
                "손으로 걸어나가 푸시업 후 돌아오세요.",
                "手で歩かせてプランクから1回押し上げ、戻ります。");

        putDict(new String[]{"marching_in_place"},
                "Dậm chân tại chỗ", "Marching in Place", "제자리 걸음", "그場行進",
                "Dậm chân tại chỗ nâng cao đầu gối đều đặn.",
                "Lift knees high in a controlled steady marching rhythm.",
                "제자리에서 무릎을 높이 들어 올리며 천천히 걸으세요.",
                "その場で膝を高く引き上げ、リズミカルに行進します。");

        putDict(new String[]{"scapular_pushup"},
                "Hít đất di chuyển xương bả vai", "Scapular Push-ups", "스캐퓰러 푸시업", "スキャプラ・プッシュアップ",
                "Chống tay thẳng, ép và mở hai xương bả vai.",
                "In plank, retract and protract shoulder blades without bending arms.",
                "팔을 핀 상태에서 어깨뼈만 접었다 피세요.",
                "肘を伸ばしたまま、肩甲骨だけを寄せて離します。");

        putDict(new String[]{"tricep_kickback"},
                "Đẩy tay sau Triceps Kickback", "Tricep Kickback", "트라이셉 킥백", "トライセップ・キックバック",
                "Gập người, duỗi cẳng tay ra sau siết cơ tay sau.",
                "Hinge forward and extend arm backward to contract triceps.",
                "상체를 숙이고 팔을 뒤로 펴서 삼두근을 수축하세요.",
                "上体を倒し、肘を固定して腕を後ろに伸ばします。");
    }

    private static void putDict(String[] keys, String vi, String en, String ko, String ja,
                                String dVi, String dEn, String dKo, String dJa) {
        ExerciseInfo info = new ExerciseInfo(vi, en, ko, ja, dVi, dEn, dKo, dJa);
        for (String k : keys) {
            DICTIONARY.put(k.toLowerCase(Locale.US).trim(), info);
        }
    }

    public static String getLanguage(Context context) {
        if (context == null) return "vi";
        return new SettingsStore(context).getLanguageCode();
    }

    public static String extractFilenameKey(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.trim().isEmpty() || mediaUrl.contains("placeholder")) {
            return "";
        }
        String url = mediaUrl.trim();
        int lastSlash = url.lastIndexOf('/');
        String filename = (lastSlash >= 0) ? url.substring(lastSlash + 1) : url;
        int dotIndex = filename.indexOf('.');
        if (dotIndex > 0) {
            filename = filename.substring(0, dotIndex);
        }
        return filename.toLowerCase(Locale.US).trim();
    }

    private static ExerciseInfo findInDictionaryByRawName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) return null;
        String lower = rawName.toLowerCase(Locale.US).trim();

        for (ExerciseInfo info : DICTIONARY.values()) {
            if (info.nameVi != null && (lower.contains(info.nameVi.toLowerCase(Locale.US)) || info.nameVi.toLowerCase(Locale.US).contains(lower))) {
                return info;
            }
            if (info.nameEn != null && (lower.contains(info.nameEn.toLowerCase(Locale.US)) || info.nameEn.toLowerCase(Locale.US).contains(lower))) {
                return info;
            }
        }
        return null;
    }

    public static String toTitleCase(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String[] words = input.replace('_', ' ').replace('-', ' ').trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) {
                sb.append(w.substring(1).toLowerCase(Locale.US));
            }
        }
        return sb.toString();
    }

    public static String getLocalizedExerciseName(Context context, String rawName, String mediaUrl) {
        if (rawName == null && mediaUrl == null) return "";
        String lang = getLanguage(context);

        String key = extractFilenameKey(mediaUrl);
        ExerciseInfo info = !key.isEmpty() ? DICTIONARY.get(key) : null;

        if (info == null && rawName != null && !rawName.trim().isEmpty()) {
            info = findInDictionaryByRawName(rawName);
        }

        if (info != null) {
            switch (lang) {
                case "en": return info.nameEn;
                case "ko": return info.nameKo;
                case "ja": return info.nameJa;
                default:
                    return (rawName != null && !rawName.trim().isEmpty()) ? rawName.trim() : info.nameVi;
            }
        }

        // Fallback keyword parsing if key not in dictionary
        return fallbackNameTranslation(context, rawName, mediaUrl, lang);
    }

    public static String getLocalizedExerciseName(Context context, String rawName) {
        return getLocalizedExerciseName(context, rawName, null);
    }

    public static String getLocalizedExerciseName(Context context, Object exercise) {
        if (exercise == null) return "";
        String name = "";
        String mediaUrl = null;

        if (exercise instanceof com.homefit.app.data.local.entity.ExerciseEntity) {
            com.homefit.app.data.local.entity.ExerciseEntity e = (com.homefit.app.data.local.entity.ExerciseEntity) exercise;
            name = e.name;
            mediaUrl = e.mediaUrl;
        } else if (exercise instanceof com.homefit.app.data.remote.dto.ExerciseDto) {
            com.homefit.app.data.remote.dto.ExerciseDto d = (com.homefit.app.data.remote.dto.ExerciseDto) exercise;
            name = d.name;
            mediaUrl = d.mediaUrl;
        } else if (exercise instanceof com.homefit.app.data.local.model.CustomWorkoutItem) {
            com.homefit.app.data.local.model.CustomWorkoutItem c = (com.homefit.app.data.local.model.CustomWorkoutItem) exercise;
            name = c.exerciseName;
            mediaUrl = c.mediaUrl;
        } else if (exercise instanceof com.homefit.app.data.local.entity.WorkoutItemEntity) {
            com.homefit.app.data.local.entity.WorkoutItemEntity w = (com.homefit.app.data.local.entity.WorkoutItemEntity) exercise;
            name = w.exerciseName;
            mediaUrl = w.exerciseMediaUrl;
        } else if (exercise instanceof com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto) {
            com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto l = (com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto) exercise;
            name = l.name;
            mediaUrl = l.mediaUrl;
        } else if (exercise instanceof String) {
            name = (String) exercise;
        }

        return getLocalizedExerciseName(context, name, mediaUrl);
    }

    private static String fallbackNameTranslation(Context context, String rawName, String mediaUrl, String lang) {
        String name = (rawName != null) ? rawName.trim() : "";
        if ("vi".equals(lang)) return name;

        String key = extractFilenameKey(mediaUrl);

        int start = name.indexOf('(');
        int end = name.indexOf(')');
        String englishInside = "";
        if (start >= 0 && end > start) {
            englishInside = name.substring(start + 1, end).trim();
            if (englishInside.toLowerCase(Locale.US).startsWith("hoặc ")) {
                englishInside = englishInside.substring(5).trim();
            }
        }

        if ("en".equals(lang)) {
            if (!englishInside.isEmpty()) return englishInside;
            if (!key.isEmpty() && !"crunches".equalsIgnoreCase(key)) return toTitleCase(key);
            return name;
        }

        String searchBase = englishInside.isEmpty() ? name : englishInside;
        String lowerBase = searchBase.toLowerCase(Locale.US);

        if ("ko".equals(lang)) {
            if (lowerBase.contains("stretch") || lowerBase.contains("giãn")) return !englishInside.isEmpty() ? englishInside : "스트레칭";
            if (lowerBase.contains("circle") || lowerBase.contains("xoay")) return !englishInside.isEmpty() ? englishInside : "돌리기";
            if (lowerBase.contains("plank")) return "플랭크";
            if (lowerBase.contains("burpee")) return "버피";
            if (lowerBase.contains("cobra")) return "코브라 스트레칭";
            if (lowerBase.contains("crunch")) return "크런치";
            if (!englishInside.isEmpty()) return englishInside;
            if (!key.isEmpty() && !"crunches".equalsIgnoreCase(key)) return toTitleCase(key);
            return name;
        }

        if ("ja".equals(lang)) {
            if (lowerBase.contains("stretch") || lowerBase.contains("giãn")) return !englishInside.isEmpty() ? englishInside : "ストレッチ";
            if (lowerBase.contains("circle") || lowerBase.contains("xoay")) return !englishInside.isEmpty() ? englishInside : "サークル";
            if (lowerBase.contains("plank")) return "プランク";
            if (lowerBase.contains("burpee")) return "バーピー";
            if (lowerBase.contains("cobra")) return "コブラストレッチ";
            if (lowerBase.contains("crunch")) return "クランチ";
            if (!englishInside.isEmpty()) return englishInside;
            if (!key.isEmpty() && !"crunches".equalsIgnoreCase(key)) return toTitleCase(key);
            return name;
        }

        return name;
    }

    public static String getLocalizedExerciseDescription(Context context, String rawDesc, String rawName, String mediaUrl) {
        String lang = getLanguage(context);

        String key = extractFilenameKey(mediaUrl);
        ExerciseInfo info = !key.isEmpty() ? DICTIONARY.get(key) : null;

        if (info == null && rawName != null && !rawName.trim().isEmpty()) {
            info = findInDictionaryByRawName(rawName);
        }

        if (info != null) {
            switch (lang) {
                case "en": return info.descEn;
                case "ko": return info.descKo;
                case "ja": return info.descJa;
                default:
                    return (rawDesc != null && !rawDesc.trim().isEmpty() && !rawDesc.contains("Toàn thân"))
                            ? rawDesc.trim() : info.descVi;
            }
        }

        if ("vi".equals(lang)) {
            return (rawDesc != null && !rawDesc.trim().isEmpty()) ? rawDesc.trim() : "Giữ tư thế chuẩn, siết chặt cơ và thở đều.";
        }

        if (rawDesc != null) {
            if (rawDesc.contains("Giữ tư thế chuẩn")) {
                switch (lang) {
                    case "en": return "Maintain proper form, engage your core, and breathe steadily.";
                    case "ko": return "바른 자세를 유지하고 코어에 힘을 주며 고르게 호흡하세요.";
                    case "ja": return "正しい姿勢を保ち、体幹を意識して一定のペースで呼吸します。";
                }
            }
            if (rawDesc.contains("Nhóm cơ tác động chính:")) {
                String muscle = rawDesc.replace("Nhóm cơ tác động chính:", "").trim();
                String locMuscle = getLocalizedTarget(context, muscle);
                switch (lang) {
                    case "en": return "Target Muscle Group: " + locMuscle;
                    case "ko": return "주요 자극 부위: " + locMuscle;
                    case "ja": return "ターゲット部位: " + locMuscle;
                }
            }
        }

        switch (lang) {
            case "en": return "Maintain proper form, engage your core, and breathe steadily.";
            case "ko": return "바른 자세를 유지하고 코어에 힘을 주며 고르게 호흡하세요.";
            case "ja": return "正しい姿勢を保ち、体幹を意識して一定のペースで呼吸します。";
            default: return "Giữ tư thế chuẩn, siết chặt cơ và thở đều.";
        }
    }

    public static String getLocalizedExerciseDescription(Context context, String rawDesc) {
        return getLocalizedExerciseDescription(context, rawDesc, null, null);
    }

    public static String getLocalizedExerciseDescription(Context context, Object exercise) {
        if (exercise == null) return "";
        String desc = "";
        String name = "";
        String mediaUrl = null;

        if (exercise instanceof com.homefit.app.data.local.entity.ExerciseEntity) {
            com.homefit.app.data.local.entity.ExerciseEntity e = (com.homefit.app.data.local.entity.ExerciseEntity) exercise;
            desc = e.description;
            name = e.name;
            mediaUrl = e.mediaUrl;
        } else if (exercise instanceof com.homefit.app.data.remote.dto.ExerciseDto) {
            com.homefit.app.data.remote.dto.ExerciseDto d = (com.homefit.app.data.remote.dto.ExerciseDto) exercise;
            desc = d.description;
            name = d.name;
            mediaUrl = d.mediaUrl;
        } else if (exercise instanceof com.homefit.app.data.local.model.CustomWorkoutItem) {
            com.homefit.app.data.local.model.CustomWorkoutItem c = (com.homefit.app.data.local.model.CustomWorkoutItem) exercise;
            if (c.instructions != null && !c.instructions.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String s : c.instructions) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("• ").append(s);
                }
                desc = sb.toString();
            } else {
                desc = c.target;
            }
            name = c.exerciseName;
            mediaUrl = c.mediaUrl;
        } else if (exercise instanceof com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto) {
            com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto l = (com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto) exercise;
            if (l.instructions != null && !l.instructions.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String s : l.instructions) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("• ").append(s);
                }
                desc = sb.toString();
            } else {
                desc = l.target;
            }
            name = l.name;
            mediaUrl = l.mediaUrl;
        }

        return getLocalizedExerciseDescription(context, desc, name, mediaUrl);
    }

    public static String getLocalizedTarget(Context context, String rawTarget) {
        if (rawTarget == null) return "";
        String lang = getLanguage(context);
        String t = rawTarget.trim().toLowerCase(Locale.US);

        if (t.contains("bụng") || t.contains("abs")) {
            switch (lang) {
                case "en": return "Abs";
                case "ko": return "복근";
                case "ja": return "腹筋";
            }
        }
        if (t.contains("ngực") || t.contains("chest")) {
            switch (lang) {
                case "en": return "Chest";
                case "ko": return "가슴";
                case "ja": return "胸";
            }
        }
        if (t.contains("tay") || t.contains("arms")) {
            switch (lang) {
                case "en": return "Arms";
                case "ko": return "팔";
                case "ja": return "腕";
            }
        }
        if (t.contains("chân") || t.contains("legs") || t.contains("mông")) {
            switch (lang) {
                case "en": return "Legs & Glutes";
                case "ko": return "다리 & 둔근";
                case "ja": return "脚 & 臀部";
            }
        }
        if (t.contains("lưng") || t.contains("back")) {
            switch (lang) {
                case "en": return "Back";
                case "ko": return "등";
                case "ja": return "背中";
            }
        }
        if (t.contains("vai") || t.contains("shoulder")) {
            switch (lang) {
                case "en": return "Shoulders";
                case "ko": return "어깨";
                case "ja": return "肩";
            }
        }
        if (t.contains("toàn thân") || t.contains("full")) {
            switch (lang) {
                case "en": return "Full Body";
                case "ko": return "전신";
                case "ja": return "全身";
            }
        }
        if (t.contains("khởi động") || t.contains("warmup")) {
            switch (lang) {
                case "en": return "Warm-up";
                case "ko": return "준비 운동";
                case "ja": return "ウォーミングアップ";
            }
        }
        if (t.contains("giãn cơ") || t.contains("stretch")) {
            switch (lang) {
                case "en": return "Stretching";
                case "ko": return "스트레칭";
                case "ja": return "ストレッチ";
            }
        }
        return rawTarget;
    }

    public static String getLocalizedTargetCaption(Context context, String rawTarget) {
        String locTarget = getLocalizedTarget(context, rawTarget);
        String lang = getLanguage(context);
        switch (lang) {
            case "en": return "Primary Target Muscle: " + locTarget;
            case "ko": return "주요 자극 부위: " + locTarget;
            case "ja": return "ターゲット部位: " + locTarget;
            default: return "Nhóm cơ tác động chính: " + locTarget;
        }
    }

    public static String getLocalizedGroup(Context context, String groupKey) {
        if (groupKey == null) return "";
        String lang = getLanguage(context);
        String key = groupKey.toLowerCase().trim();

        if (key.contains("bung") || key.contains("abs")) {
            switch (lang) {
                case "en": return "Abs";
                case "ko": return "복근";
                case "ja": return "腹筋";
                default: return "Bụng";
            }
        }
        if (key.contains("tay") || key.contains("arms")) {
            switch (lang) {
                case "en": return "Arms";
                case "ko": return "팔";
                case "ja": return "腕";
                default: return "Tay";
            }
        }
        if (key.contains("nguc") || key.contains("chest")) {
            switch (lang) {
                case "en": return "Chest";
                case "ko": return "가슴";
                case "ja": return "胸";
                default: return "Ngực";
            }
        }
        if (key.contains("chan") || key.contains("legs") || key.contains("mong")) {
            switch (lang) {
                case "en": return "Legs";
                case "ko": return "다리";
                case "ja": return "脚";
                default: return "Chân";
            }
        }
        if (key.contains("lung") || key.contains("back")) {
            switch (lang) {
                case "en": return "Back";
                case "ko": return "등";
                case "ja": return "背中";
                default: return "Lưng";
            }
        }
        if (key.contains("toan_than") || key.contains("fullbody") || key.contains("full_body")) {
            switch (lang) {
                case "en": return "Full Body";
                case "ko": return "전신";
                case "ja": return "全身";
                default: return "Toàn thân";
            }
        }
        return groupKey;
    }

    public static String getLocalizedDifficulty(Context context, String difficulty) {
        if (difficulty == null) return "";
        String lang = getLanguage(context);
        String diff = difficulty.toLowerCase().trim();

        if (diff.contains("easy") || diff.contains("dễ")) {
            switch (lang) {
                case "en": return "Easy";
                case "ko": return "쉬움";
                case "ja": return "初級";
                default: return "Dễ";
            }
        }
        if (diff.contains("hard") || diff.contains("khó")) {
            switch (lang) {
                case "en": return "Hard";
                case "ko": return "어려움";
                case "ja": return "上級";
                default: return "Khó";
            }
        }
        switch (lang) {
            case "en": return "Medium";
            case "ko": return "보통";
            case "ja": return "中級";
            default: return "Trung bình";
        }
    }

    public static String getLocalizedRepsSets(Context context, String rawRepsSets) {
        if (rawRepsSets == null) return "";
        String lang = getLanguage(context);
        String text = rawRepsSets.trim();

        if ("vi".equals(lang)) return text;

        if (text.contains("giây")) {
            switch (lang) {
                case "en": text = text.replace("giây", "s"); break;
                case "ko": text = text.replace("giây", "초"); break;
                case "ja": text = text.replace("giây", "秒"); break;
            }
        }
        if (text.contains("lần")) {
            switch (lang) {
                case "en": text = text.replace("lần", "reps"); break;
                case "ko": text = text.replace("lần", "회"); break;
                case "ja": text = text.replace("lần", "回"); break;
            }
        }
        if (text.contains("mỗi bên")) {
            switch (lang) {
                case "en": text = text.replace("mỗi bên", "each side"); break;
                case "ko": text = text.replace("mỗi bên", "양쪽 각각"); break;
                case "ja": text = text.replace("mỗi bên", "左右各"); break;
            }
        }
        if (text.contains("mỗi chiều")) {
            switch (lang) {
                case "en": text = text.replace("mỗi chiều", "each direction"); break;
                case "ko": text = text.replace("mỗi chiều", "각 방향"); break;
                case "ja": text = text.replace("mỗi chiều", "各方向"); break;
            }
        }
        if (text.contains("Bài tập") || text.contains("bài tập")) {
            switch (lang) {
                case "en": text = text.replaceAll("(?i)bài tập", "Exercises"); break;
                case "ko": text = text.replaceAll("(?i)bài tập", "개 운동"); break;
                case "ja": text = text.replaceAll("(?i)bài tập", "種目"); break;
            }
        }
        if (text.contains("phút")) {
            switch (lang) {
                case "en": text = text.replace("phút", "min"); break;
                case "ko": text = text.replace("phút", "분"); break;
                case "ja": text = text.replace("phút", "分"); break;
            }
        }
        return text;
    }

    public static String getLocalizedDurationAndCount(Context context, int durationMin, int count) {
        String lang = getLanguage(context);
        switch (lang) {
            case "en":
                return durationMin + " min • " + count + " Exercises";
            case "ko":
                return durationMin + "분 • " + count + "개 운동";
            case "ja":
                return durationMin + "分 • " + count + "種目";
            default:
                return durationMin + " phút • " + count + " Bài tập";
        }
    }

    public static String getLocalizedChallengeTitle(Context context) {
        String lang = getLanguage(context);
        switch (lang) {
            case "en": return "SELF CHALLENGE";
            case "ko": return "자신과의 도전";
            case "ja": return "セルフチャレンジ";
            default: return "THỬ THÁCH BẢN THÂN";
        }
    }

    public static String getLocalizedDayTitle(Context context, int day, String typeName) {
        String lang = getLanguage(context);
        String localizedType = typeName;
        if (typeName != null) {
            if (typeName.contains("Lưng & Bụng")) {
                switch (lang) {
                    case "en": localizedType = "Back & Abs"; break;
                    case "ko": localizedType = "등 & 복근"; break;
                    case "ja": localizedType = "背中 & 腹筋"; break;
                }
            } else if (typeName.contains("Toàn thân")) {
                switch (lang) {
                    case "en": localizedType = "Full Body"; break;
                    case "ko": localizedType = "전신"; break;
                    case "ja": localizedType = "全身"; break;
                }
            } else if (typeName.contains("Ngực & Tay")) {
                switch (lang) {
                    case "en": localizedType = "Chest & Arms"; break;
                    case "ko": localizedType = "가슴 & 팔"; break;
                    case "ja": localizedType = "胸 & 腕"; break;
                }
            }
        }
        switch (lang) {
            case "en": return "Day " + day + " - " + localizedType;
            case "ko": return day + "일차 - " + localizedType;
            case "ja": return day + "日目 - " + localizedType;
            default: return "Ngày " + day + " - " + localizedType;
        }
    }

    public static String localizeWorkoutTitle(Context context, String rawTitle) {
        if (rawTitle == null) return "";
        String lang = getLanguage(context);
        String title = rawTitle.trim();

        if ("vi".equals(lang)) return title;

        String lower = title.toLowerCase(Locale.US);

        String baseTitle = title;
        String diffSuffix = "";

        if (lower.endsWith("easy") || lower.endsWith("dễ")) {
            diffSuffix = getLocalizedDifficulty(context, "Easy");
            baseTitle = title.substring(0, title.length() - 4).trim();
        } else if (lower.endsWith("medium") || lower.endsWith("trung bình")) {
            diffSuffix = getLocalizedDifficulty(context, "Medium");
            if (lower.endsWith("medium")) baseTitle = title.substring(0, title.length() - 6).trim();
            else baseTitle = title.substring(0, title.length() - 10).trim();
        } else if (lower.endsWith("hard") || lower.endsWith("khó")) {
            diffSuffix = getLocalizedDifficulty(context, "Hard");
            if (lower.endsWith("hard")) baseTitle = title.substring(0, title.length() - 4).trim();
            else baseTitle = title.substring(0, title.length() - 3).trim();
        }

        String lowerBase = baseTitle.toLowerCase(Locale.US);
        String locBase = baseTitle;

        if (lowerBase.contains("đốt cháy calo toàn thân")) {
            switch (lang) {
                case "en": locBase = "Full Body Calorie Burn"; break;
                case "ko": locBase = "전신 칼로리 소모"; break;
                case "ja": locBase = "全身カロリー消費"; break;
            }
        } else if (lowerBase.contains("đốt mỡ nhẹ nhàng")) {
            switch (lang) {
                case "en": locBase = "Light Fat Burn"; break;
                case "ko": locBase = "가벼운 지방 소모"; break;
                case "ja": locBase = "マイルド脂肪燃焼"; break;
            }
        } else if (lowerBase.contains("hiit toàn thân")) {
            switch (lang) {
                case "en": locBase = "Full Body HIIT"; break;
                case "ko": locBase = "전신 HIIT"; break;
                case "ja": locBase = "全身HIIT"; break;
            }
        } else if (lowerBase.contains("sức bền toàn thân")) {
            switch (lang) {
                case "en": locBase = "Full Body Endurance"; break;
                case "ko": locBase = "전신 지구력"; break;
                case "ja": locBase = "全身持久力"; break;
            }
        } else if (lowerBase.contains("toàn thân cân bằng")) {
            switch (lang) {
                case "en": locBase = "Balanced Full Body"; break;
                case "ko": locBase = "균형 잡힌 전신"; break;
                case "ja": locBase = "バランス全身"; break;
            }
        } else if (lowerBase.contains("toàn thân + lõi")) {
            switch (lang) {
                case "en": locBase = "Full Body & Core"; break;
                case "ko": locBase = "전신 & 코어"; break;
                case "ja": locBase = "全身 & コア"; break;
            }
        } else if (lowerBase.contains("ngực & tay")) {
            switch (lang) {
                case "en": locBase = "Chest & Arms"; break;
                case "ko": locBase = "가슴 & 팔"; break;
                case "ja": locBase = "胸 & 腕"; break;
            }
        } else if (lowerBase.contains("chân & mông")) {
            switch (lang) {
                case "en": locBase = "Legs & Glutes"; break;
                case "ko": locBase = "하체 & 둔근"; break;
                case "ja": locBase = "脚 & 臀部"; break;
            }
        } else if (lowerBase.contains("thân trên + lõi")) {
            switch (lang) {
                case "en": locBase = "Upper Body & Core"; break;
                case "ko": locBase = "상체 & 코어"; break;
                case "ja": locBase = "上半身 & コア"; break;
            }
        } else if (lowerBase.contains("thử thách sức mạnh chân")) {
            switch (lang) {
                case "en": locBase = "Leg Strength Challenge"; break;
                case "ko": locBase = "하체 근력 도전"; break;
                case "ja": locBase = "脚力チャレンジ"; break;
            }
        } else if (lowerBase.contains("6 phút với tabata")) {
            switch (lang) {
                case "en": locBase = "6 Min Tabata"; break;
                case "ko": locBase = "6분 타바타"; break;
                case "ja": locBase = "6分タバ타"; break;
            }
        } else if (lowerBase.contains("kéo giãn lưng")) {
            switch (lang) {
                case "en": locBase = "Back Stretch"; break;
                case "ko": locBase = "등 스트레칭"; break;
                case "ja": locBase = "背中ストレッチ"; break;
            }
        } else if (lowerBase.contains("tập ngực & tay")) {
            switch (lang) {
                case "en": locBase = "Chest & Arms"; break;
                case "ko": locBase = "가슴 & 팔 운동"; break;
                case "ja": locBase = "胸 & 腕"; break;
            }
        } else if (lowerBase.contains("cơ bụng") || lowerBase.equalsIgnoreCase("bụng")) {
            switch (lang) {
                case "en": locBase = "Abs"; break;
                case "ko": locBase = "복근"; break;
                case "ja": locBase = "腹筋"; break;
            }
        } else if (lowerBase.contains("cơ ngực") || lowerBase.equalsIgnoreCase("ngực")) {
            switch (lang) {
                case "en": locBase = "Chest"; break;
                case "ko": locBase = "가슴"; break;
                case "ja": locBase = "胸"; break;
            }
        } else if (lowerBase.contains("cơ tay") || lowerBase.equalsIgnoreCase("tay")) {
            switch (lang) {
                case "en": locBase = "Arms"; break;
                case "ko": locBase = "팔"; break;
                case "ja": locBase = "腕"; break;
            }
        } else if (lowerBase.contains("cơ chân") || lowerBase.equalsIgnoreCase("chân")) {
            switch (lang) {
                case "en": locBase = "Legs"; break;
                case "ko": locBase = "다리"; break;
                case "ja": locBase = "脚"; break;
            }
        } else if (lowerBase.contains("toàn thân")) {
            switch (lang) {
                case "en": locBase = "Full Body"; break;
                case "ko": locBase = "전신"; break;
                case "ja": locBase = "全身"; break;
            }
        }

        return diffSuffix.isEmpty() ? locBase : locBase + " " + diffSuffix;
    }

    public static String getLocalizedWorkoutDescription(Context context, String groupKey, String title) {
        String lang = getLanguage(context);
        String combined = ((groupKey != null ? groupKey : "") + " " + (title != null ? title : "")).toLowerCase(Locale.US);

        if (combined.contains("thân trên") || combined.contains("upper") || combined.contains("nguc") || combined.contains("chest") || combined.contains("tay") || combined.contains("arm")) {
            if (combined.contains("nguc") || combined.contains("chest") || combined.contains("tay") || combined.contains("arm")) {
                switch (lang) {
                    case "en":
                        return "Build a strong chest and defined arms! Focus on effective push-up variations and bodyweight arm exercises designed to increase chest and arm strength without equipment.";
                    case "ko":
                        return "가슴과 팔 근육을 단단하게 다지세요! 다양한 푸시업 변형과 체중 팔 운동에 집중하여 기구 없이 가슴과 팔의 힘과 부피를 키워줍니다.";
                    case "ja":
                        return "たくましい胸と引き締まった腕を作りましょう！自重腕立て伏せや腕のエクササイズに集中し、器具なしで胸と腕の筋力・ボリュームを高めます。";
                    default:
                        return "Phát triển vòm ngực rộng và bắp tay săn chắc! Tập trung vào các biến thể chống đẩy và bài tập tay bodyweight hiệu quả giúp tăng kích thước và sức mạnh ngực tay mà không cần dụng cụ.";
                }
            } else {
                switch (lang) {
                    case "en":
                        return "Optimize upper body and core strength! This routine combines chest, shoulder, arm, and abdominal exercises to build lean muscle, improve posture, and enhance overall endurance.";
                    case "ko":
                        return "상체와 코어 근력을 극대화하세요! 가슴, 어깨, 팔, 복근 운동을 조합하여 탄탄한 근육을 만들고 자세 개선 및 전반적인 지구력을 향상시킵니다.";
                    case "ja":
                        return "上半身と体幹の筋力を極めましょう！胸・肩・腕・腹筋を組み合わせたエクササイズで、引き締まった筋肉を作り、姿勢改善と持久力アップをサポートします。";
                    default:
                        return "Tối ưu hóa sức mạnh thân trên và vùng lõi! Buổi tập kết hợp các bài tập ngực, vai, tay và cơ bụng giúp phát triển cơ bắp săn chắc, cải thiện tư thế và tăng cường sức bền tổng thể.";
                }
            }
        } else if (combined.contains("chan") || combined.contains("leg") || combined.contains("mong") || combined.contains("glute")) {
            switch (lang) {
                case "en":
                    return "Build powerful legs and toned glutes! Squat, Lunge, and Glute Bridge variations strengthen lower body muscles, burn high calories, and boost overall athletic performance.";
                case "ko":
                    return "강력한 하체와 탄탄한 힙라인을 만드세요! 스쿼트, 런지, 글루트 브릿지 동작으로 하체 근력을 강화하고 칼로리를 효과적으로 소모합니다.";
                case "ja":
                    return "力強い脚と引き締まったヒップを作りましょう！スクワットやランジ、ヒップブリッジで下半身を強化し、高いカロリー消費と体力向上を実現します。";
                default:
                    return "Xây dựng đôi chân khỏe khoắn và vòng 3 săn chắc! Các bài tập Squat, Lunge và Cầu mông giúp tăng cường sức mạnh chi dưới, đốt cháy calo vượt trội và nâng cao thể lực.";
            }
        } else if (combined.contains("bung") || combined.contains("abs") || combined.contains("loi") || combined.contains("core")) {
            switch (lang) {
                case "en":
                    return "Start your core journey today! This workout focuses on the entire abdominal region, helping you build core strength and definition. With every movement, you get closer to a strong and toned midsection!";
                case "ko":
                    return "오늘부터 복근 운동을 시작하세요! 이 운동은 전체 복부 부위에 집중하여 코어 힘과 선명도를 키우는 데 도움을 줍니다. 매 동작마다 강하고 탄탄한 복근에 한 걸음 더 가까워집니다!";
                case "ja":
                    return "今日から腹筋トレーニングを始めましょう！このワークアウトは腹部全体に集中し、体幹の筋力と引き締め効果を高めます。一回ごとの動作で、強くて引き締まった腹筋に近づきます！";
                default:
                    return "Khởi đầu hành trình cơ bụng ngay hôm nay! Bài tập này tập trung vào toàn bộ vùng bụng, giúp bạn tạo sự sắc nét và xây dựng sức mạnh cốt lõi. Với mỗi động tác, bạn sẽ tiến gần hơn đến một vùng bụng mạnh mẽ và săn chắc!";
            }
        } else {
            switch (lang) {
                case "en":
                    return "Burn full-body calories and boost overall fitness! This compound workout targets all major muscle groups for maximum calorie burn, lean body conditioning, and agility.";
                case "ko":
                    return "전신 칼로리를 소모하고 체력을 증진하세요! 주요 근육군을 모두 자극하는 복합 운동으로 균형 잡힌 몸매와 유연성을 선사합니다.";
                case "ja":
                    return "全身のカロリーを消費し総合的な体力をアップ！主要な筋肉群をバランスよく刺激し、引き締まった体と柔軟性を手に入れましょう。";
                default:
                    return "Đốt cháy calo toàn thân và nâng cao thể lực tổng thể! Chuỗi bài tập phối hợp liên hoàn giúp tác động đến tất cả các nhóm cơ chính, mang lại vóc dáng thon gọn và dẻo dai.";
            }
        }
    }

    public static String localizeSubtitle(Context context, int durationMin, String difficulty) {
        String localizedDiff = getLocalizedDifficulty(context, difficulty);
        String lang = getLanguage(context);
        switch (lang) {
            case "en":
                return durationMin + " min • " + localizedDiff;
            case "ko":
                return durationMin + "분 • " + localizedDiff;
            case "ja":
                return durationMin + "分 • " + localizedDiff;
            default:
                return durationMin + " phút • " + localizedDiff;
        }
    }

    public static String getLocalizedStateHeader(Context context, Object state) {
        String lang = getLanguage(context);
        String stateStr = state != null ? state.toString() : "";
        if (stateStr.contains("PREPARING") || stateStr.contains("Chuẩn bị")) {
            switch (lang) {
                case "en": return "Get Ready";
                case "ko": return "준비";
                case "ja": return "準備";
                default: return "Chuẩn bị";
            }
        } else if (stateStr.contains("EXERCISING") || stateStr.contains("Tập luyện")) {
            switch (lang) {
                case "en": return "Workout";
                case "ko": return "운동 중";
                case "ja": return "運動中";
                default: return "Tập luyện";
            }
        } else if (stateStr.contains("PAUSED") || stateStr.contains("Tạm dừng")) {
            switch (lang) {
                case "en": return "Paused";
                case "ko": return "일시 정지";
                case "ja": return "一時停止";
                default: return "Tạm dừng";
            }
        } else if (stateStr.contains("RESTING") || stateStr.contains("Nghỉ ngơi")) {
            switch (lang) {
                case "en": return "Rest";
                case "ko": return "휴식";
                case "ja": return "休憩";
                default: return "Nghỉ ngơi";
            }
        }
        return stateStr;
    }

    public static String getLocalizedProgressHeader(Context context, int current, int total) {
        String lang = getLanguage(context);
        switch (lang) {
            case "en": return "Exercise " + current + " / " + total;
            case "ko": return "운동 " + current + " / " + total;
            case "ja": return "種目 " + current + " / " + total;
            default: return "Bài " + current + " / " + total;
        }
    }

    public static String getLocalizedNextHeader(Context context, int next, int total) {
        String lang = getLanguage(context);
        switch (lang) {
            case "en": return "NEXT UP " + next + "/" + total;
            case "ko": return "다음 운동 " + next + "/" + total;
            case "ja": return "次の運動 " + next + "/" + total;
            default: return "TIẾP THEO " + next + "/" + total;
        }
    }
}
