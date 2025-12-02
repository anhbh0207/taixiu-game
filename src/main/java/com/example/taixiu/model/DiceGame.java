package com.example.taixiu.model;

import java.util.List;
import java.util.Random;

public class DiceGame {

    public enum Side {
        TAI, XIU
    }

    private static final Random RANDOM = new Random();

    // Kiểm tra sum là Tài hay Xỉu
    public static Side judgeSide(int sum) {
        // Luật đơn giản: 4–10 = Xỉu, 11–17 = Tài
        if (sum >= 4 && sum <= 10) {
            return Side.XIU;
        } else {
            return Side.TAI;
        }
    }

    // Sinh side thắng với kiểu random "phức tạp" dựa trên history (streak / zigzag)
    public static Side randomWinningSide(List<Side> history) {
        double r = RANDOM.nextDouble();

        if (history == null || history.isEmpty()) {
            // Lần đầu: random đều
            return RANDOM.nextBoolean() ? Side.TAI : Side.XIU;
        }

        Side last = history.get(history.size() - 1);

        // 0.3: lặp lại (tạo chuỗi 2–4 lần liên tiếp cùng màu)
        if (r < 0.3) {
            return last;
        }

        // 0.3 tiếp theo: đảo (tạo zig-zag: T-X-T-X...)
        if (r < 0.6) {
            return last == Side.TAI ? Side.XIU : Side.TAI;
        }

        // Còn lại random bất kỳ
        return RANDOM.nextBoolean() ? Side.TAI : Side.XIU;
    }

    // Roll 3 viên xúc xắc sao cho sum ra đúng side thắng
    public static int[] rollDiceForSide(Side side) {
        while (true) {
            int d1 = RANDOM.nextInt(6) + 1;
            int d2 = RANDOM.nextInt(6) + 1;
            int d3 = RANDOM.nextInt(6) + 1;
            int sum = d1 + d2 + d3;
            Side judged = judgeSide(sum);
            if (judged == side) {
                return new int[]{d1, d2, d3};
            }
        }
    }
}
