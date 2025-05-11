package com.example.accelerometer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

public class MazeView extends View {

    // 迷宫布局 (0=路径, 1=墙壁, 2=起点, 3=终点)
    private int[][] maze = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 0, 0, 0, 0, 1, 0, 0, 1},
            {1, 1, 1, 1, 0, 0, 1, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 1, 0, 0, 1},
            {1, 0, 1, 1, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 1, 1, 1, 1, 0, 0, 1},
            {1, 1, 0, 1, 0, 0, 0, 0, 1, 1},
            {1, 0, 0, 1, 0, 1, 1, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 1, 0, 3, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    private float cellSize; // 单元格大小
    private float ballRadius; // 球的半径
    private float ballX, ballY; // 球的位置
    private float startX, startY; // 起点位置
    private float endX, endY; // 终点位置
    
    private Paint wallPaint; // 墙壁绘制
    private Paint pathPaint; // 路径绘制
    private Paint ballPaint; // 球绘制
    private Paint startPaint; // 起点绘制
    private Paint endPaint; // 终点绘制
    
    private boolean gameWon = false; // 游戏是否胜利
    
    // 物理参数
    private static final float FRICTION = 0.8f; // 摩擦系数
    private static final float SENSITIVITY = 0.6f; // 灵敏度
    private float velocityX = 0, velocityY = 0; // 球的速度

    public MazeView(Context context) {
        super(context);
        init();
    }

    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 初始化画笔
        wallPaint = new Paint();
        wallPaint.setColor(Color.rgb(33, 33, 33));
        
        pathPaint = new Paint();
        pathPaint.setColor(Color.rgb(240, 240, 240));
        
        ballPaint = new Paint();
        ballPaint.setColor(Color.rgb(231, 76, 60));
        ballPaint.setAntiAlias(true);
        
        startPaint = new Paint();
        startPaint.setColor(Color.rgb(46, 204, 113));
        
        endPaint = new Paint();
        endPaint.setColor(Color.rgb(52, 152, 219));
        
        // 找到起点和终点
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                if (maze[y][x] == 2) { // 起点
                    startX = x;
                    startY = y;
                } else if (maze[y][x] == 3) { // 终点
                    endX = x;
                    endY = y;
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // 计算单元格大小，取较小值以确保迷宫完全适配
        cellSize = Math.min(w / maze[0].length, h / maze.length);
        ballRadius = cellSize * 0.3f; // 球的半径为单元格大小的30%
        
        // 初始化球位置在起点中心
        ballX = (startX + 0.5f) * cellSize;
        ballY = (startY + 0.5f) * cellSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制迷宫
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                float left = x * cellSize;
                float top = y * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;
                
                if (maze[y][x] == 1) { // 墙壁
                    canvas.drawRect(left, top, right, bottom, wallPaint);
                } else if (maze[y][x] == 0) { // 路径
                    canvas.drawRect(left, top, right, bottom, pathPaint);
                } else if (maze[y][x] == 2) { // 起点
                    canvas.drawRect(left, top, right, bottom, startPaint);
                } else if (maze[y][x] == 3) { // 终点
                    canvas.drawRect(left, top, right, bottom, endPaint);
                }
            }
        }
        
        // 绘制球
        canvas.drawCircle(ballX, ballY, ballRadius, ballPaint);
    }

    public void updateBallPosition(float accelerationX, float accelerationY) {
        if (gameWon) return; // 如果游戏已胜利，不再更新位置
        
        // 物理模拟：加速度变化速度
        velocityX += accelerationX * SENSITIVITY;
        velocityY += accelerationY * SENSITIVITY;
        
        // 应用摩擦力
        velocityX *= FRICTION;
        velocityY *= FRICTION;
        
        // 计算新位置
        float newX = ballX + velocityX;
        float newY = ballY + velocityY;
        
        // 检测碰撞
        if (canMoveTo(newX, newY)) {
            ballX = newX;
            ballY = newY;
            
            // 检查是否到达终点
            checkWinCondition();
        } else {
            // 碰到墙壁，速度反弹并减小
            velocityX *= -0.5f;
            velocityY *= -0.5f;
        }
        
        // 重绘
        invalidate();
    }
    
    private boolean canMoveTo(float x, float y) {
        // 转换为迷宫坐标
        int mazeX = (int)(x / cellSize);
        int mazeY = (int)(y / cellSize);
        
        // 检查边界
        if (mazeX < 0 || mazeX >= maze[0].length || mazeY < 0 || mazeY >= maze.length) {
            return false;
        }
        
        // 碰撞检测 - 考虑球的半径
        // 检查球的四个方向是否有墙
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // 跳过中心点
                
                float checkX = x + i * ballRadius;
                float checkY = y + j * ballRadius;
                
                int checkMazeX = (int)(checkX / cellSize);
                int checkMazeY = (int)(checkY / cellSize);
                
                // 确保检查点在范围内
                if (checkMazeX >= 0 && checkMazeX < maze[0].length && 
                    checkMazeY >= 0 && checkMazeY < maze.length) {
                    if (maze[checkMazeY][checkMazeX] == 1) { // 墙
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private void checkWinCondition() {
        // 转换为迷宫坐标
        int currentMazeX = (int)(ballX / cellSize);
        int currentMazeY = (int)(ballY / cellSize);
        
        // 检查是否在终点位置
        if (currentMazeX == (int)endX && currentMazeY == (int)endY) {
            gameWon = true;
            
            // 显示胜利消息
            Toast.makeText(getContext(), "恭喜！您成功通过迷宫！", Toast.LENGTH_SHORT).show();
        }
    }
} 