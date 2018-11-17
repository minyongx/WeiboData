package cn.edu.zjut.myong.com.weibo.operator;

public abstract class PageOperatorAttempt {

    public abstract void run() throws Exception;

    public abstract void update();

    public void start(int n) {
        for (int i = 0; i < n; i++) {
            try {
                run();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                update();
            }
        }
    }
}
