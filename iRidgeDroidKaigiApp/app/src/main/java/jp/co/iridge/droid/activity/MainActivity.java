package jp.co.iridge.droid.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.viro.core.ARAnchor;
import com.viro.core.ARImageTarget;
import com.viro.core.ARNode;
import com.viro.core.ARScene;
import com.viro.core.AndroidViewTexture;
import com.viro.core.Animation;
import com.viro.core.AnimationTimingFunction;
import com.viro.core.AsyncObject3DListener;
import com.viro.core.CameraListener;
import com.viro.core.DeviceNotCompatibleException;
import com.viro.core.Material;
import com.viro.core.Node;
import com.viro.core.Object3D;
import com.viro.core.OmniLight;
import com.viro.core.Quad;
import com.viro.core.Spotlight;
import com.viro.core.Texture;
import com.viro.core.Vector;
import com.viro.core.ViroView;
import com.viro.core.ViroViewARCore;

import java.util.Arrays;

import jp.co.iridge.droid.R;
import jp.co.iridge.droid.util.ApplicationUtil;

public class MainActivity extends AppCompatActivity {

    private static final String PATH_ANCHOR_IMAGE = "anchor/logo.png";
    private static final String PATH_HDR_TEXTURE = "file:///android_asset/wakanda_360.hdr";
    private static final String PATH_TV_VRX = "file:///android_asset/model/tv.vrx";
    private static final String PATH_DROID_VRX = "file:///android_asset/model/droid.vrx";
    private static final String PATH_SADAKO_VRX = "file:///android_asset/model/sadako.vrx";

    protected ViroView mViroView;
    private ImageView mFitToScanView;
    private View mCommentView;
    private ARScene mScene;
    private ARImageTarget mImageTarget;

    private Node mTvNode;
    private Node mDroidNode;
    private Node mSadakoNode;

    private boolean mTvObjLoaded = false;
    private boolean mDroidObjLoaded = false;
    private boolean mSadakoObjLoaded = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mViroView = new ViroViewARCore(this, new ViroViewARCore.StartupListener() {
                @Override public void onSuccess() {
                    doRender();
                }
                @Override public void onFailure(ViroViewARCore.StartupError error, String errorMessage) {
                    throw new DeviceNotCompatibleException();
                }
            });

            mFitToScanView = new ImageView(this);
            mFitToScanView.setImageResource(R.drawable.fittoscan);
            mFitToScanView.setScaleType(ImageView.ScaleType.FIT_XY);
            mFitToScanView.setAdjustViewBounds(true);
            mFitToScanView.setVisibility(View.GONE);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.rightMargin = (int)(getResources().getDimension(R.dimen.fit_to_scam_margin) + 0.5f);
            lp.leftMargin = (int)(getResources().getDimension(R.dimen.fit_to_scam_margin) + 0.5f);
            mViroView.addView(mFitToScanView, lp);
            setContentView(mViroView);
        } catch (DeviceNotCompatibleException de) {
            ApplicationUtil.showToast(getApplicationContext(), getString(R.string.error_evice_is_not_compatiblity));
            finish();
        }
    }

    @Override protected void onStart() {
        super.onStart();
        mViroView.onActivityStarted(this);
    }

    @Override protected void onResume() {
        super.onResume();
        mViroView.onActivityResumed(this);
    }

    @Override protected void onPause() {
        super.onPause();
        mViroView.onActivityPaused(this);
    }

    @Override protected void onStop() {
        super.onStop();
        mViroView.onActivityStopped(this);
    }

    /**
     * レンダリング開始。
     */
    private void doRender() {

        mScene = new ARScene();
        mViroView.setScene(mScene);

        Bitmap targetBmp = ApplicationUtil.getBitmapFromAssets(this, PATH_ANCHOR_IMAGE);
        mImageTarget = new ARImageTarget(targetBmp, ARImageTarget.Orientation.Up, 0.188f);
        //mImageTarget = new ARImageTarget(targetBmp, ARImageTarget.Orientation.Up, 0.2f);
        mScene.addARImageTarget(mImageTarget);

        Node groupNode = new Node();
        initTvModel(groupNode);
        initSadakoNode(groupNode);
        initDroidNode(groupNode);
        initDroidComment(mDroidNode);
        groupNode.setVisible(true);
        mScene.getRootNode().addChildNode(groupNode);

        Texture environment = Texture.loadRadianceHDRTexture(Uri.parse(PATH_HDR_TEXTURE));
        mScene.setLightingEnvironment(environment);
    }

    /**
     * TVモデルの初期化。
     * @param groupNode
     */
    private void initTvModel(Node groupNode) {

        Object3D tvObject = new Object3D();
        tvObject.setScale(new Vector(0.00f, 0.00f, 0.00f));
        tvObject.loadModel(mViroView.getViroContext(), Uri.parse(PATH_TV_VRX), Object3D.Type.FBX, new AsyncObject3DListener() {
            @Override public void onObject3DFailed(String s) {}
            @Override public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {
                mTvObjLoaded = true;
                trackImageNodeTargets();
            }
        });
        tvObject.addChildNode(initLightingNode(200));
        groupNode.addChildNode(tvObject);
        tvObject.setVisible(false);
        mTvNode = tvObject;
    }

    /**
     * ドロイドモデルの初期化。
     * @param groupNode
     */
    private void initDroidNode(Node groupNode) {
        Object3D fbxDroidNode = new Object3D();
        fbxDroidNode.setScale(new Vector(0.08f, 0.08f, 0.08f));
        fbxDroidNode.loadModel(mViroView.getViroContext(), Uri.parse(PATH_DROID_VRX), Object3D.Type.FBX, new AsyncObject3DListener() {
            @Override public void onObject3DFailed(String s) {}
            @Override public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {
                mDroidObjLoaded = true;
                trackImageNodeTargets();
            }
        });
        groupNode.addChildNode(fbxDroidNode);
        fbxDroidNode.setVisible(false);
        mDroidNode = fbxDroidNode;
    }

    /**
     * 貞子モデルの初期化。
     * @param groupNode
     */
    private void initSadakoNode(Node groupNode) {
        Object3D fbxDroidNode = new Object3D();
        fbxDroidNode.setScale(new Vector(0.08f, 0.08f, 0.08f));
        fbxDroidNode.loadModel(mViroView.getViroContext(), Uri.parse(PATH_SADAKO_VRX), Object3D.Type.FBX, new AsyncObject3DListener() {
            @Override public void onObject3DFailed(String s) {}
            @Override public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {
                mSadakoObjLoaded = true;
                trackImageNodeTargets();
            }
        });
        fbxDroidNode.addChildNode(initLightingNode(200));
        groupNode.addChildNode(fbxDroidNode);
        fbxDroidNode.setVisible(false);
        mSadakoNode = fbxDroidNode;
    }

    /**
     * ドロイドコメントを初期化する。
     * @param node
     */
    private void initDroidComment(Node node) {

        mCommentView = getLayoutInflater().inflate(R.layout.view_comment, null);
        mCommentView.setVisibility(View.INVISIBLE);
        int pxWidth = 800;
        int pxHeight = 720;
        boolean isAccelerated = true;
        AndroidViewTexture androidTexture = new AndroidViewTexture(mViroView, pxWidth, pxHeight, isAccelerated);
        androidTexture.attachView(mCommentView);

        final Material material = new Material();
        material.setDiffuseTexture(androidTexture);

        Quad surface = new Quad(1f, 0.89f);
        surface.setMaterials(Arrays.asList(material));
        Node surfaceNode = new Node();
        surfaceNode.setGeometry(surface);
        surfaceNode.setPosition(new Vector(0.6f,0.3f,5.5f));

        // Add the Surface to the scene.
        node.addChildNode(surfaceNode);
    }

    /**
     * アンカーイメージトラッキング開始。
     */
    private void trackImageNodeTargets() {
        mFitToScanView.setVisibility(View.VISIBLE);
        mScene.setListener(new ARScene.Listener() {
            @Override public void onTrackingInitialized() {}
            @Override public void onAmbientLightUpdate(float lightIntensity, Vector color) {}
            @Override public void onTrackingUpdated(ARScene.TrackingState state, ARScene.TrackingStateReason reason) {}
            @Override public void onAnchorUpdated(ARAnchor anchor, ARNode arNode) {}
            @Override public void onAnchorFound(final ARAnchor anchor, ARNode arNode) {
                String anchorId = anchor.getAnchorId();
                if (!mImageTarget.getId().equalsIgnoreCase(anchorId)
                        || !mTvObjLoaded || !mDroidObjLoaded || !mSadakoObjLoaded) {
                    return;
                }

                mViroView.setCameraListener(new CameraListener() {
                    @Override public void onTransformUpdate(Vector position, Vector rotation, Vector forward) {
                        mViroView.setCameraListener(null);
                        mFitToScanView.setVisibility(View.GONE);
                        final Vector rot = new Vector(0, rotation.y, 0);
                        final Vector pos = new Vector(anchor.getPosition().x, anchor.getPosition().y, anchor.getPosition().z);

                        mTvNode.setPosition(pos);
                        mTvNode.setRotation(rot);
                        mTvNode.setVisible(true);
                        ApplicationUtil.animateScale(mTvNode, 500, new Vector(0.07f, 0.07f, 0.07f), AnimationTimingFunction.EaseInEaseOut, null);

                        mSadakoNode.setPosition(pos);
                        mSadakoNode.setRotation(rot);
                        mSadakoNode.setVisible(false);

                        mDroidNode.setPosition(pos);
                        mDroidNode.setRotation(rot);
                        mDroidNode.setVisible(false);

                        startSadakoAppearAnimation(3000);

                    }
                });

                mScene.removeARImageTarget(mImageTarget);
                mScene.setListener(null);
            }

            @Override public void onAnchorRemoved(ARAnchor anchor, ARNode arNode) {
                ApplicationUtil.showToast(getApplicationContext(), "Anchor removed.");

                String anchorId = anchor.getAnchorId();
                if (!mImageTarget.getId().equalsIgnoreCase(anchorId)) {
                    return;
                }
            }
        });
    }

    /**
     * 貞子出現アニメーション開始。
     * @param delay 開始遅延時間
     */
    private void startSadakoAppearAnimation(int delay) {
        final Animation animationAppear = mSadakoNode.getAnimation("Armature|SadakoAppear");
        animationAppear.setListener(new Animation.Listener() {
            @Override public void onAnimationStart(Animation animation) {
                mSadakoNode.setVisible(true);
            }
            @Override public void onAnimationFinish(Animation animation, boolean canceled) {
                startDroidAppearAnimation(0);
            }
        });
        animationAppear.setDelay(delay);
        animationAppear.play();
    }

    /**
     * ドロイド出現アニメーション開始。
     * @param delay 開始遅延時間
     */
    private void startDroidAppearAnimation(int delay) {
        mSadakoNode.setVisible(false);
        mTvNode.setVisible(false);
        mDroidNode.addChildNode(initLightingNode(3000));
        mSadakoNode.removeAllChildNodes();
        mTvNode.removeAllChildNodes();
        mDroidNode.setVisible(true);

        final Animation animationAppear = mDroidNode.getAnimation("Armature|DroidAppear");
        animationAppear.setListener(new Animation.Listener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationFinish(Animation animation, boolean canceled) {
                mViroView.setCameraListener(new CameraListener() {
                    @Override public void onTransformUpdate(Vector position, Vector rotation, Vector forward) {
                        final Vector rot = new Vector(0, rotation.y, rotation.z);
                        mDroidNode.setRotation(rot);
                        mCommentView.setVisibility(View.VISIBLE);
                        mViroView.setCameraListener(null);
                        startIdleAnimation(1000);
                    }
                });
            }
        });
        animationAppear.setDelay(delay);
        animationAppear.play();
    }

    /**
     * ドロイドアイドルアニメーション開始。
     * @param delay 開始遅延時間
     */
    private void startIdleAnimation(int delay) {
        final Animation animationAppear = mDroidNode.getAnimation("Armature|DroidIdle");
        animationAppear.setListener(new Animation.Listener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationFinish(Animation animation, boolean b) {
                // loop
                mCommentView.setVisibility(View.INVISIBLE);
                startIdleAnimation(0);
            }
        });
        animationAppear.setDelay(delay);
        animationAppear.play();
    }

    /**
     * ライト設定。
     * @return
     */
    private Node initLightingNode(int spotIntensity) {
        Vector omniLightPositions [] = {    new Vector(-3, 3, 0.3),
                new Vector(3, 3, 1),
                new Vector(-3,-3,1),
                new Vector(3, -3, 1)};

        // omnilight
        Node lightingNode = new Node();
        for (Vector pos : omniLightPositions){
            final OmniLight light = new OmniLight();
            light.setPosition(pos);
            light.setColor(Color.parseColor("#FFFFFF"));
            light.setIntensity(400);
            light.setAttenuationStartDistance(6);
            light.setAttenuationEndDistance(9);

            lightingNode.addLight(light);
        }

        // spotlight
        Spotlight spotLight = new Spotlight();
        spotLight.setPosition(new Vector(0, 0, 15));
        spotLight.setDirection(new Vector(0, 0, -1));

        spotLight.setColor(Color.parseColor("#FFFFFF"));
        spotLight.setIntensity(spotIntensity);
        spotLight.setShadowOpacity(0.4f);
        spotLight.setShadowMapSize(2048);
        spotLight.setShadowNearZ(2f);
        spotLight.setShadowFarZ(7f);
        spotLight.setInnerAngle(5);
        spotLight.setOuterAngle(20);
        spotLight.setCastsShadow(true);
        lightingNode.addLight(spotLight);
        return lightingNode;
    }
}
