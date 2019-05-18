# Introduction
다음 조건을 만족하는 안드로이드 어플리케이션 개발
- G 시작점의 볼을 중앙으로 드래그 하면 성공
- G 이미지를 벗어나면 실패
- G 시작점의 볼이 끝점으로 온전히 이동하지 않으면 실패

Toy Project  
Author: ceedlive


## Documentation


### Development Environment
+ Windows 10
+ Android Studio 3.3.2
+ Visual Studio Code
+ Gradle
+ GitHub
    + Repository: https://github.com/CEEDLIVE/AndroidGesture.git


### Test Devices
+ Galaxy S6 (Android 7.0)
+ Galaxy Note9 (Android 9.0)


### Flow
 - 커스텀뷰 생성
 - 상하좌우 여백이 없는 G 모양의 이미지 리소스 추가
 - 화면의 정중앙에 위치하도록 레이아웃 구현
 - G 모양 내부를 한붓 그리기로 통과하는 패스 추가
 - 패스 위를 움직이는 이미지 리소스 추가
 - 커스텀뷰 터치 이벤트 핸들러 추가
 - 마무리
 

### Logic
- First
    - 연속된 직선들을 n 방향으로 구간을 나누고 360도 중에서 해당 구간으로 뻗어 나가는 직선을 인식하여 번호를 부여함
    - 부여된 번호가 미리 정의된 연속된 번호들과 일치 하는지 확인
    - 확인된 값을 근거로 사용자의 입력을 처리하는 로직 구현
    - References
        - https://samarian.tistory.com/entry/%ED%84%B0%EC%B9%98-%EC%A0%9C%EC%8A%A4%EC%B2%98-%ED%8C%A8%ED%84%B4-%EC%9D%B8%EC%8B%9D-%EB%AA%A8%EB%93%88
        - https://github.com/myung-nyun/touchgesturepattern 
- Second
    - 문자 모양의 G 내부를 한붓 그리기로 통과하는 패스 생성
    - 커스텀뷰 터치 이벤트 발생 시 조건에 맞게 패스 위를 통과하며 움직이는 비트맵 이미지 구현
    - References
        - [![Android Kotlin animation move image along path](http://img.youtube.com/vi/14pvfdGh18Y/0.jpg
)](https://www.youtube.com/watch?v=14pvfdGh18Y)
            - https://github.com/betranthanh/android-kotlin-animation-move-image-along-path
- Third
    - G 내부에 제스처 유효성 검증을 위한 투명 렉트 생성
        - 시작점, 북쪽, 서쪽, 남쪽, 동쪽, 끝점 순
    - 렉트 객체의 위치와 사이즈 계산은 다음 이미지를 참고
        - https://ko.wikipedia.org/wiki/%EC%82%BC%EA%B0%81%ED%95%A8%EC%88%98#/media/File:Unit_circle_angles.svg
        - http://www.gisdeveloper.co.kr/?p=4611 (중간 화살표 이미지 참고)


### Follow Field Naming Conventions

안드로이드 오픈소스는 아래와 같은 Naming Convention 을 따릅니다.

+ Non-public, non-static field names start with m. (public, static이 아닌 것에는 m을 붙여라.(m은 멤버변수의 m입니다.))
+ Static field names start with s. (static에는 s를 붙여라)
+ Other fields start with a lower case letter. (나머지 모든 필드에는 소문자로 시작한다.)
+ Public static final fields (constants) are ALL_CAPS_WITH_UNDERSCORES. (public static final fields 에는 _를 붙히고 모든 문자를 대문자로 써라)

```java
//example
public class MyClass {
    public static final int SOME_CONSTANT = 42;
    public int publicField;
    private static MyClass sSingleton;
    int mPackagePrivate;
    private int mPrivate;
    protected int mProtected;
}
```

http://source.android.com/source/code-style.html#follow-field-naming-conventions  
위주소에 들어가보시면 더 자세한 규칙들을 보실 수 있어요.


### Bookmarks

#### Touch, Gesture
- [터치 제스처 패턴 인식 모듈](https://samarian.tistory.com/entry/%ED%84%B0%EC%B9%98-%EC%A0%9C%EC%8A%A4%EC%B2%98-%ED%8C%A8%ED%84%B4-%EC%9D%B8%EC%8B%9D-%EB%AA%A8%EB%93%88)
    - https://github.com/myung-nyun/touchgesturepattern
- [사용자 View에서 사각형 내부 Touch 판단하기](http://ssiso.net/cafe/club/club1/board1/content.php?board_code=android%7CandroidNormal&idx=34671&club=android)
- [Android getting ACTION_UP without ACTION_DOWN](https://stackoverflow.com/questions/27501524/android-getting-action-up-without-action-down)
- [안드로이드 터치 (MOTIONEVENT)](https://stuban.tistory.com/69)
- [Java Code Examples for android.view.MotionEvent.ACTION_UP](https://www.programcreek.com/java-api-examples/?class=android.view.MotionEvent&method=ACTION_UP)
- [[컴][안드로이드] invalidate() 과 postInvalidate() 의 차이](http://i5on9i.blogspot.com/2012/11/invalidate-postinvalidate.html)
- [TOUCHEVENT 사용법](https://tony-programming.tistory.com/entry/TouchEvent-%EC%82%AC%EC%9A%A9%EB%B2%95)
    - 커스텀 뷰와 레이아웃을 어떻게 사용하면 좋을지 좋은 가이드가 된 포스팅
- [안드로이드(Android) onDraw() 를 이용해 스크린터치로 원그리기](https://mainia.tistory.com/1277)
- [터치이벤트(TouchEvent)로 비트맵이미지 이동시키기 2](https://biig.tistory.com/22)
- [[Android] onTouch 이벤트 /뷰 이동 / 뷰 드래그 앤 드랍 / 레이아웃 밖으로 못나가게 하기](https://m.blog.naver.com/PostView.nhn?blogId=tkddlf4209&logNo=220734131855&proxyReferer=https%3A%2F%2Fwww.google.com%2F)
- [[안드로이드] 터치로 이미지 크기 조정, 이동, 회전 시키기](http://blog.naver.com/PostView.nhn?blogId=khs7515&logNo=20155940183)
- [사용자 View에서 사각형 내부 Touch 판단하기](http://ssiso.net/cafe/club/club1/board1/content.php?board_code=android%7CandroidNormal&idx=34671&club=android)
- [how to get color at the spot(or pixel) of a image on touch event in android](https://stackoverflow.com/questions/14920303/how-to-get-color-at-the-spotor-pixel-of-a-image-on-touch-event-in-android)
- [선택 가능한 항목의 전경(Foreground)에 터치 피드백 쉽게 적용하기 (Selector/Ripple Drawable)](https://www.androidhuman.com/tip/ui/2016/01/18/support_selectable_item_background/)
- [안드로이드의 터치 이벤트에서 이미지의 색상 (또는 픽셀)을 얻는 방법](https://stackoverrun.com/ko/q/4042744)
- [view에서 bitmap에 터치가 들어왔는지 확인하는법?](https://www.androidpub.com/1422403)
- [안드로이드/Android 터치 이벤트(Touch Event) 에서 Touch, LongTouch 동시에 구현 하기 ~!](https://arabiannight.tistory.com/entry/333)
- [android LongClick Event 누른 상태 구현](http://blog.naver.com/PostView.nhn?blogId=wonminst&logNo=90120177224)
- [Android 에서 Long Click 직접 구현하기](https://blog.naver.com/mbyn33/90116108888)
- [[android] Long Press ( Long Click ) 직접 구현하기](https://aroundck.tistory.com/2043)
- [view 에서 Long Click 을 처리 할 수 있는 방법](http://skyswim42.egloos.com/tag/ViewConfiguration/page/1)
- [[Android] Touch Event를 이용한 LongTouch 구현](https://pheadra.tistory.com/entry/Android-Touch-Event%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-LongTouch-%EA%B5%AC%ED%98%84)
- [Detecting a long press with Android](https://stackoverflow.com/questions/7919865/detecting-a-long-press-with-android)
- [[Android] onTouch 사용시, "ACTION_DOWN"만 들어오는 문제](https://mydevromance.tistory.com/18)


#### RGB
- [rgb를 integer형으로 변환에 대하여](https://www.androidpub.com/1724187)
- [안드로이드에서 color integer값을 hex String으로 바꾸는 방법](https://hashcode.co.kr/questions/1284/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C%EC%97%90%EC%84%9C-color-integer%EA%B0%92%EC%9D%84-hex-string%EC%9C%BC%EB%A1%9C-%EB%B0%94%EA%BE%B8%EB%8A%94-%EB%B0%A9%EB%B2%95)
- [how to convert RGB to HEXADECIMAL color in android? [duplicate]](https://stackoverflow.com/questions/17739502/how-to-convert-rgb-to-hexadecimal-color-in-android)
- [how to convert rgb color to int in java](https://stackoverflow.com/questions/18022364/how-to-convert-rgb-color-to-int-in-java)
- [안드로이드 setpixels getpixels 정리](http://blog.naver.com/PostView.nhn?blogId=ebadatv&logNo=220927570601&parentCategoryNo=&categoryNo=60&viewDate=&isShowPopularPosts=false&from=section)
- [안드로이드에서 픽셀 색상을 얻는 방법?](https://codeday.me/ko/qa/20190314/67053.html)
- [[안드로이드/Android]Palette,Swatch를 활용해서 이미지의 테마색 가져오기](https://gun0912.tistory.com/67)


#### View
- [[Android] 뷰 위에 그래픽을 그리기](http://ilililililililililili.blogspot.com/2013/07/android_16.html)
- [Protip. Inflating layout for your custom view](https://trickyandroid.com/protip-inflating-layout-for-your-custom-view/)
- [Creating custom and compound views in Android - Tutorial](https://www.vogella.com/tutorials/AndroidCustomViews/article.html)
- [Android Custom View Tutorial](https://www.raywenderlich.com/142-android-custom-view-tutorial)
- [Custom Views: make your android app stand out](https://android.jlelse.eu/custom-views-make-your-android-app-stand-out-fa386b506860?gi=b7e128bcfdcb)
- [How to display an ImageView in a custom view in Android](https://stackoverflow.com/questions/3714582/how-to-display-an-imageview-in-a-custom-view-in-android)
- [안드로이드(Android) 경고 - Custom view… overrides onTouchEvent but not performClick](https://mainia.tistory.com/1272)
- [How to get the rectangle of the Drawable of ImageView on Android?](https://stackoverflow.com/questions/8987580/how-to-get-the-rectangle-of-the-drawable-of-imageview-on-android)
- [안드로이드 코드로 View에서 Drawable을 Background로 적용(Programmatically)](https://yoo2yoo2yoo2.tistory.com/4)
- [(3) 안드로이드(Android) 의 ImageView 레이아웃 옵션과 사용예제들 - 2](https://mainia.tistory.com/473)


#### MotionLayout
- [MotionLayout으로 만드는 안드로이드 애니메이션](https://www.charlezz.com/?p=717)
- [Lottie](https://airbnb.design/lottie/)
- [[GitHub] airbnb/lottie-android](https://github.com/airbnb/lottie-android)
- [[GitHub] CEEDLIVE/android-ConstraintLayoutExamples](https://github.com/CEEDLIVE/android-ConstraintLayoutExamples)
- [[Android, Lottie] Lottie 를 사용한 애니메이션 효과](https://black-jin0427.tistory.com/97)
- [Yanolja + MotionLayout #Part1 ~ MotionLayout](http://pluu.github.io/blog/android/2019/01/17/motionlayout-part1/)
- [Could not set unknown property 'useAndroidX' for object of type com.android.build.gradle.internal.dsl.BaseAppModuleExtension](https://stackoverflow.com/questions/53857191/could-not-set-unknown-property-useandroidx-for-object-of-type-com-android-buil/53857213)
- [Android ConstraintLayout 2.0 with Animation](https://medium.com/@eyegochild/android-constraintlayout-2-0-with-animation-a01ffd4bbe97)
- [Introduction to MotionLayout (part I)](https://medium.com/google-developers/introduction-to-motionlayout-part-i-29208674b10d)
- [Creating Animations With MotionLayout for Android](https://code.tutsplus.com/tutorials/creating-animations-with-motionlayout-for-android--cms-31497)
- [MotionLayout Tutorial For Android: Getting Started](https://www.raywenderlich.com/8883-motionlayout-tutorial-for-android-getting-started)
- [How can I scale textView inside parent view with motion layout?](https://stackoverflow.com/questions/53603009/how-can-i-scale-textview-inside-parent-view-with-motion-layout)
- [Android Weekly #319](https://breadboy.tistory.com/265)
- [Defining motion paths in MotionLayout](https://medium.com/google-developers/defining-motion-paths-in-motionlayout-6095b874d37)
- [Android MotionLayout Examples](https://www.zoftino.com/android-motionlayout-examples)
- [Failed linking file resources when using Motion Layout KeyPosition](https://stackoverflow.com/questions/52570494/failed-linking-file-resources-when-using-motion-layout-keyposition)
    - For your information, target has been changed to motionTarget in KeyPosition and KeyAttribute
- [ConstraintLayout 2.0 with MotionLayout, with Huyen Tue Dao [Droidkast LIVE 02]](https://antonioleiva.com/constraint-motion-layout-droidkast-02/)
- [MotionLayout + ShapeOfView = 😍](https://proandroiddev.com/motionlayout-shapeofview-26a7ab10142f)
- [Exploring MotionLayout KeyCycle](https://proandroiddev.com/exploring-motionlayout-keycycle-b990d9cef5bf)
- [DroidKnights 2019 ~ MotionLayout 무릎까지 담궈보기](http://pluu.github.io/blog/android/2019/04/07/droidknights-motionlayout/)
- [MotionLayout 무릎까지 담궈보기](https://speakerdeck.com/pluu/motionlayout-mureupggaji-damgweobogi)
- [MotionLayout을 통한 코드 한줄 없이 전화효과 주기](https://blog.kmshack.kr/motionlayout/)
- [안드로이드 ConstraintLayout 사용법](https://gamjatwigim.tistory.com/40)
- [드로이드 나이츠(Droid Knights) 2019 참관 후기](https://kofboy2000.tistory.com/42)
- [How To Create Animations For Android Using MotionLayout](http://www.wearemobilefirst.com/blog/how-to-create-animations-for-android-using-motionlayout/)
- [Deep dive into MotionLayout](https://www.slideshare.net/hagikuratakeshi/deep-dive-into-motionlayout)


#### Canvas, Paint
- [Android canvas. contains(x, y) for an oval?](https://stackoverflow.com/questions/30729759/android-canvas-containsx-y-for-an-oval)
- [자바 그래픽 네모상자(사각형) 그리기 drawRect(), fillRect(), clearRect()](https://aiden1004.tistory.com/entry/%EC%9E%90%EB%B0%94-%EA%B7%B8%EB%9E%98%ED%94%BD-%EB%84%A4%EB%AA%A8%EC%83%81%EC%9E%90%EC%82%AC%EA%B0%81%ED%98%95-%EA%B7%B8%EB%A6%AC%EA%B8%B0-drawRect-fillRect-clearRect)
- [How to draw a rectangle in Android (using onDraw method of View)](https://alvinalexander.com/android/how-to-draw-rectangle-in-android-view-ondraw-canvas)
- [Android canvas draw rectangle](https://stackoverflow.com/questions/7344497/android-canvas-draw-rectangle)
- [How can I get the bitmap of the canvas I get in onDraw?](https://stackoverflow.com/questions/10847728/how-can-i-get-the-bitmap-of-the-canvas-i-get-in-ondraw)
- [Object allocation during draw/layout?](https://stackoverflow.com/questions/16472529/object-allocation-during-draw-layout)
- [Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);](https://medium.com/@hector6872/avoid-object-allocations-during-draw-layout-operations-preallocate-and-reuse-instead-b686239ef3fa)
- [what is the relation between Canvas and Bitmap?](https://stackoverflow.com/questions/6605632/what-is-the-relation-between-canvas-and-bitmap)
- [canvas 화면을 Bitmap으로 저장 완결판..](https://javaexpert.tistory.com/71)
- [[Android, Canvas, Paint] 안드로이드 캔버스 기본 예제](https://black-jin0427.tistory.com/144)
- [캔버스를 사용하여 화면 가운데에 이미지를 배치하는 방법](https://stackoverrun.com/ko/q/1395012)
- [[Android, Canvas, Paint] 안드로이드 캔버스 기본 예제](https://black-jin0427.tistory.com/144)
- [[ Android Opengl es 2.0 ] Vertex배열을 이용하여 간단한 도형 만들기](https://gogorchg.tistory.com/entry/Android-Opengl-es-20-Vertex%EB%B0%B0%EC%97%B4%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%98%EC%97%AC-%EA%B0%84%EB%8B%A8%ED%95%9C-%EB%8F%84%ED%98%95-%EB%A7%8C%EB%93%A4%EA%B8%B0)
- [Canvas.save() Canvas.restore()](https://simsimjae.tistory.com/269)
- [Canvas(캔버스), Paint(페인트) 1편](https://itpangpang.tistory.com/94)
- [[안드로이드] android canvas triangle, star, heart 예제 (TouchEvent)](https://milkissboy.tistory.com/59)
- [[안드로이드/Android] 안드로이드 Canvas](https://jwandroid.tistory.com/183)
- [안드로이드 뷰에 그려보기 -1- (원 그리기)](https://infinitesail.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EB%B7%B0%EC%97%90-%EC%95%84%EB%AC%B4%EA%B1%B0%EB%82%98-%EB%A7%89-%EA%B7%B8%EB%A6%AC%EA%B8%B0-1)


#### Bitmap
- [[코딩] Bitmap 객체 사용하기](http://egloos.zum.com/surprisen/v/2354943)
- [안드로이드 뷰(View) 2](https://hyunssssss.tistory.com/98)
- [[graphic] RGB565와 RGBA8888에 대해서](https://hanburn.tistory.com/140)
- [ImageView에서 비트맵 받는 법](https://hashcode.co.kr/questions/782/imageview%EC%97%90%EC%84%9C-%EB%B9%84%ED%8A%B8%EB%A7%B5-%EB%B0%9B%EB%8A%94-%EB%B2%95)
- [ImageView의 사진을 Bitmap으로 가져오기](https://kworks.tistory.com/338)
- [Android - ImageView: setImageBitmap VS setImageDrawable](https://stackoverflow.com/questions/12001793/android-imageview-setimagebitmap-vs-setimagedrawable)
- [Android get points of BitMap when I touch on a Image View](https://stackoverflow.com/questions/7077530/android-get-points-of-bitmap-when-i-touch-on-a-image-view)
- [Get bitmap color on touched position in ImageView](http://android-er.blogspot.com/2013/08/get-bitmap-color-on-touched-position-in.html)
- [How to get the X and Y touch location inside the Bitmap](https://stackoverflow.com/questions/33519081/how-to-get-the-x-and-y-touch-location-inside-the-bitmap)
- [how to get color at the spot(or pixel) of a image on touch event in android](https://stackoverflow.com/questions/14920303/how-to-get-color-at-the-spotor-pixel-of-a-image-on-touch-event-in-android)
- [Android imageview get pixel color from scaled image](https://stackoverflow.com/questions/12496339/android-imageview-get-pixel-color-from-scaled-image)
- [Animators may only be run on Looper threads Android](https://stackoverflow.com/questions/37689903/animators-may-only-be-run-on-looper-threads-android)
- [[Java/Android] 간단하게 사용하는 Bitmap 리사이즈(Resize) 예제](http://theeye.pe.kr/archives/1380)
- [Bitmap resize and crop](https://bbulog.tistory.com/25)
- [How to get a Bitmap from VectorDrawable](https://stackoverflow.com/questions/36513854/how-to-get-a-bitmap-from-vectordrawable)
- [BitmapDrawable deprecated alternative](https://stackoverflow.com/questions/9978884/bitmapdrawable-deprecated-alternative)
- [안드로이드 비트맵(Bitmap) 리사이즈](http://egloos.zum.com/pavecho/v/7210478)
- [안드로이드 비트맵 리사이즈 - android bitmap resize](http://egloos.zum.com/javalove/v/67828)
- [Set transparent background of an imageview on Android](https://stackoverflow.com/questions/1492554/set-transparent-background-of-an-imageview-on-android/37323776)
- [Draw on a canvas with Bitmap as Background 1](https://stackoverflow.com/questions/14518387/draw-on-a-canvas-with-bitmap-as-background)
- [Draw on a canvas with Bitmap as Background 2](https://stackoverflow.com/questions/14518387/draw-on-a-canvas-with-bitmap-as-background)
- [[ Android ] 비트맵에 이미지나 뭔가를 그릴 때 뜨는 에러!](https://gogorchg.tistory.com/entry/Android-%EB%B9%84%ED%8A%B8%EB%A7%B5%EC%97%90-%EC%9D%B4%EB%AF%B8%EC%A7%80%EB%82%98-%EB%AD%94%EA%B0%80%EB%A5%BC-%EA%B7%B8%EB%A6%B4-%EB%95%8C-%EB%9C%A8%EB%8A%94-%EC%97%90%EB%9F%AC)
- [[안드로이드] Bitmap을 불러와서 수정할 때 나는 예외(Immutable bitmap passed to Canvas constructor)](https://eskeptor.tistory.com/73)
- [이미지로 캔버스에 비트 맵 드로잉을 저장하는 방법? 코드를 확인 하시겠습니까?](https://codeday.me/ko/qa/20190401/209803.html)
- [안드로이드에서 bitmap으로 넣은 이미지 중심 좌표 구할수 있나요 ??](http://m.todayhumor.co.kr/view.php?table=total&no=10314117)
- [안드로이드 비트맵 이미지 영역부분 RGB 좌표출력 후 색상 변환](http://www.masterqna.com/android/38348/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EB%B9%84%ED%8A%B8%EB%A7%B5-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EC%98%81%EC%97%AD%EB%B6%80%EB%B6%84-rgb-%EC%A2%8C%ED%91%9C%EC%B6%9C%EB%A0%A5-%ED%9B%84-%EC%83%89%EC%83%81-%EB%B3%80%ED%99%98)
- [안드로이드 (Android) Bitmap 구현, 관리 하기](https://mainia.tistory.com/468)
- [안드로이드/Android Bitmap 정리~!](https://arabiannight.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9CAndroid-Bitmap)
- [[안드로이드] ImageView에 Bitmap 최적화 로딩하기 - OutOfMemoryError 처리](https://sharp57dev.tistory.com/24)
- [java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.graphics.Bitmap.isRecycled()' on a null object reference](https://stackoverflow.com/questions/39442020/java-lang-nullpointerexception-attempt-to-invoke-virtual-method-boolean-androi)
- [Android: How do I scale a bitmap to fit the screen size using canvas/draw bitmap?](https://stackoverflow.com/questions/38573926/android-how-do-i-scale-a-bitmap-to-fit-the-screen-size-using-canvas-draw-bitmap)
- [안드로이드(Android) 비트맵의 픽셀값을 읽어와 화면에 그리기](https://mainia.tistory.com/474)
- [[Android] bitmap을 출력한 후 touch를 이용해서 drag하는 예제](https://sdw8001.tistory.com/5)
- [[ 안드로이드 ] 그리기객체 bitmap](https://baramziny.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EA%B7%B8%EB%A6%AC%EA%B8%B0%EA%B0%9D%EC%B2%B4-bitmap)
- [안드로이드 Bitmap 그리기 질문](http://www.masterqna.com/android/17370/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-bitmap-%EA%B7%B8%EB%A6%AC%EA%B8%B0-%EC%A7%88%EB%AC%B8)
- [Bitmap 생성](https://kscho.tistory.com/entry/Bitmap-%EC%83%9D%EC%84%B1)
- [BitmapFactory 이미지 맵핑하는 방법](http://m.blog.daum.net/urlover/17049874)
- [Matrix를 이용한 이미지 확대/축소/회전/이동/skew(비틀기)](http://www.shop-wiz.com/document/android/sample5_use_matrix)
- [[ImageView] ScaleType: Matrix (예제)](https://thinking-jmini.tistory.com/28)


#### Path
- [[안드로이드/Android] 안드로이드 Path](https://jwandroid.tistory.com/184)
    - 그리기(Draw) 를 할때 사용되는 녀석 중. "덤" 정도로 생각하시면 되겠습니다.
	쉽게 말해서 도화지에 우리가 선을 긋는데 그냥 그으면 삐뚤삐뚤 해집니다. 하지만 자를 대고 선을 그으면 똑바로 그을수가 있죠. 이때 "자"의 역할을 해주는 녀석을 "Path"라 생각 하시면 되겠습니다. 도화지(Canvas)에 어떤 도형(직선, 곡선, 다각형)을 그리는데 미리 그려진 궤적 정보라고 생각하시면 됩니다. 조금 이해가 가시나요?
- [[Android/안드로이드] Canvas 에 Path 를 그려보자.](https://aroundck.tistory.com/293)
- [[ 안드로이드 ] path로 그리기](https://baramziny.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-path%EB%A1%9C-%EA%B7%B8%EB%A6%AC%EA%B8%B0)
- [[Android] Path를 이용한 그리기(Canvas)](https://sdw8001.tistory.com/2)
- [drawLine, drawPath를 써보자](https://itpangpang.xyz/247)
- [Android: How to check if a path contains touched point?](https://stackoverflow.com/questions/9588003/android-how-to-check-if-a-path-contains-touched-point)
- [path tracing 선을 그릴때 사용되는 path tracing 기법을 사용해보자!](https://medium.com/marojuns-android/path-tracing-c316e496aa2f)
- [RectF와 Path로 그리는 그림](https://developer-joe.tistory.com/23)
- [[안드로이드] Canvas를 활용해 커스텀 뷰에 선 그리기 (drawPath)](https://mparchive.tistory.com/105)
- [![Android Kotlin animation move image along path](http://img.youtube.com/vi/14pvfdGh18Y/0.jpg
)](https://www.youtube.com/watch?v=14pvfdGh18Y)
    - https://github.com/betranthanh/android-kotlin-animation-move-image-along-path
- [Playing with Paths](https://medium.com/androiddevelopers/playing-with-paths-3fbc679a6f77)
- [자바 – 맵지도 캔버스에 사용자 정의 도면](https://codeday.me/ko/qa/20190512/531612.html)
- [흐름 방향을 갖는 선형 설비에 대한 심벌 표현](http://www.gisdeveloper.co.kr/?p=4611)
- [Problem to achieve curved animation [duplicate]](https://stackoverflow.com/questions/6849554/problem-to-achieve-curved-animation/8454990#8454990)
- [Bézier curve](https://en.wikipedia.org/wiki/B%C3%A9zier_curve)
- [Animation of moving bitmap along path](http://android-er.blogspot.com/2014/05/animation-of-moving-bitmap-along-path.html)
- [Draw Path on canvas of custom View](http://android-er.blogspot.com/2014/05/draw-path-on-canvas-of-custom-view.html)
- [![Android - Rendering a Path with a Bitmap fill](http://img.youtube.com/vi/tQo4XA-FEgw/0.jpg
)](https://www.youtube.com/watch?v=tQo4XA-FEgw)
- [drawArc 부채꼴, 호를 그려보자(+RectF)](https://itpangpang.xyz/321)
- [MenuArrowAnimation 구현하기](https://laewoong.tistory.com/3)
    - https://github.com/laewoong/MenuArrowAnimation


#### Ripple
- [Ripple effect on shape drawable](https://stackoverflow.com/questions/34458482/ripple-effect-on-shape-drawable)
- [안드로이드 리플(Ripple) 효과를 활용하여 버튼 배 클릭 효과 넣기](https://beaqon.tistory.com/254)
- [How to set a ripple effect on textview or imageview on Android?](https://stackoverflow.com/questions/33477025/how-to-set-a-ripple-effect-on-textview-or-imageview-on-android)
- [이미지 위에 리플 효과 – Android](https://codeday.me/ko/qa/20190401/205343.html)


#### 삼각함수
- [삼각함수 육각형](https://zetawiki.com/wiki/%EC%82%BC%EA%B0%81%ED%95%A8%EC%88%98_%EC%9C%A1%EA%B0%81%ED%98%95)
- [코탄젠트 함수 개념 설명 그림; 삼각함수에서 Cot 그래프; Cotangent Diagram](http://mwultong.blogspot.com/2008/03/cot-cotangent-diagram.html)
- [삼각함수](https://ko.wikipedia.org/wiki/%EC%82%BC%EA%B0%81%ED%95%A8%EC%88%98)


#### Fundamental
- [potential bug using removeAll() called by a Collection on itself](https://stackoverflow.com/questions/35294351/potential-bug-using-removeall-called-by-a-collection-on-itself/35294430)
    - removeAll()' called on collection 'arVertex1' with itself as argument
- [android getIntrinsicHeight 및 getIntrinsicWidth는 무엇을 의미합니까?](https://stackoverrun.com/ko/q/3712980)
    - Intrinsic 고유한, 본질적인 (인트린식)
- [What is the unit of bitmap.getWidth() or bitmap.getHeight()](https://stackoverflow.com/questions/17063984/what-is-the-unit-of-bitmap-getwidth-or-bitmap-getheight)
- [[Android]뷰(View)의 절대좌표 구하기](http://blog.naver.com/PostView.nhn?blogId=wlsdml1103&logNo=220619687049)
- [[Android] View의 절대좌표 구하기](https://devbible.tistory.com/140)
- [getLocationOnScreen() vs getLocationInWindow()](https://stackoverflow.com/questions/17672891/getlocationonscreen-vs-getlocationinwindow)
- [Android 에서 Vector(벡터) 이미지 사용하기](https://developer88.tistory.com/151)
- [[GitHub] hyunjun/bookmarks](https://github.com/hyunjun/bookmarks/blob/master/android.md)
- [안드로이드의 새로운 레이아웃 탐구서](https://academy.realm.io/kr/posts/exploring-new-android-layouts/)
- [안드로이드 기초 정리](http://softwarearchitect.kr/blog/664){: target="_blank"}
