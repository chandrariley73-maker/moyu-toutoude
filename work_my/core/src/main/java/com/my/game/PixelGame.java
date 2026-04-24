package com.my.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.Arrays;

public class PixelGame extends ApplicationAdapter {
    SpriteBatch batch;
    OrthographicCamera camera;

    // 玩家
    Rectangle player;
    float speed = 300f;

    // 精灵表动画相关 → 全部提升为成员变量
    Texture walkSheet;
    Animation<TextureRegion> runAnimation;
    Animation<TextureRegion> jumpAnimation;
    Animation<TextureRegion> crouchAnimation;
    TextureRegion[] run; // 这里加上！
    TextureRegion[] jump; // 这里加上！
    TextureRegion[] crouch; // 这里加上！
    float stateTime;

    // 跳跃相关（真正的物理跳跃）
    float velocityY = 0; // 垂直速度
    float gravity = -800; // 重力
    float jumpPower = 450; // 跳跃力度
    boolean isOnGround = true;

    @Override
    public void create() {
        // 创建SpriteBatch实例，用于绘制精灵
        batch = new SpriteBatch();
        // 创建正交相机，设置视口大小为800x600
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);

        // 加载精灵表
        walkSheet = new Texture("2hao.png");

        int frameWidth = 77;
        int frameHeight = 192;
        TextureRegion[][] tmp = TextureRegion.split(walkSheet, frameWidth, frameHeight);
        run = tmp[0];
        jump = tmp[1];
        crouch = tmp[2];
        // 创建动画
        runAnimation = new Animation<>(0.15f, run);
        jumpAnimation = new Animation<>(0.15f, jump);
        crouchAnimation = new Animation<>(0.30f, crouch);

        stateTime = 0f;

        // 玩家
        player = new Rectangle();
        player.x = 50;
        player.y = 50;
        player.width = 64;
        player.height = 64;
    }

    @Override
/**
 * 渲染方法，用于绘制游戏画面
 * 该方法负责清除屏幕、处理玩家移动、更新动画状态以及绘制玩家角色
 */
    public void render() {
        // 和精灵表背景色完全一致
        Gdx.gl.glClearColor(0.67f, 0.69f, 0.75f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 开启透明混合，让图片边缘更自然
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 获取自上一帧以来经过的时间(秒)
        float delta = Gdx.graphics.getDeltaTime();
        // 更新状态时间，用于动画帧计算
        stateTime += delta;
        // 移动相关变量初始化
        boolean moving = false;
        boolean isCrouch = false;

        // 检测A键是否被按下，如果按下则向左移动玩家
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.x -= speed * delta;
            moving = true;
        }
        // 检测D键是否被按下，如果按下则向右移动玩家
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.x += speed * delta;
            moving = true;
        }

        // 检测W键是否被按下，如果按下则向上移动玩家
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && isOnGround) {
            velocityY = jumpPower;
            isOnGround = false;
        }

        // 应用重力
        velocityY += gravity * delta;
        player.y += velocityY * delta;

        // 地面碰撞（落到地面就停下）
        if (player.y <= 50) {
            player.y = 50;
            velocityY = 0;
            isOnGround = true;
        }

        // 检测S键是否被按下，如果按下则向下移动玩家
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            isCrouch = true; // 标记为下蹲状态
        }

        // 获取当前帧
        TextureRegion currentFrame;
        if (moving) {
            currentFrame = runAnimation.getKeyFrame(stateTime, true);
        }
        else if (!isOnGround) {
            currentFrame = jumpAnimation.getKeyFrame(stateTime, true);
        }
        else if (isCrouch) {
            currentFrame = crouchAnimation.getKeyFrame(stateTime, true);
        }
        else {
            currentFrame = crouch[0];
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(currentFrame, player.x, player.y, 32, 64);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        walkSheet.dispose();
    }
}
