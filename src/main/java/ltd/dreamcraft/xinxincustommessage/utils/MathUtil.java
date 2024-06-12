package ltd.dreamcraft.xinxincustommessage.utils;

import java.util.Stack;

/**
 * @Author: haishen668
 * @CreateTime: 2024-05-01
 * @Description: 数学运算符处理 (有bug)
 * @Version: 1.0
 */

@Deprecated
public class MathUtil {
    public static String calculateByText(String expression) {
        char[] tokens = expression.toCharArray();

        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        StringBuilder nonMathPart1 = new StringBuilder();
        StringBuilder nonMathPart2 = new StringBuilder();
        boolean readingFirstPart = true;

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ')
                continue;

            if (Character.isDigit(tokens[i]) || tokens[i] == '(' || tokens[i] == ')' || tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/') {
                // 当遇到第一个数字或操作符时，切换到读取数学表达式
                if (readingFirstPart) {
                    readingFirstPart = false;  // 切换标志，后续字符串为第二部分
                }

                if (Character.isDigit(tokens[i])) {
                    StringBuilder sb = new StringBuilder();
                    while (i < tokens.length && (Character.isDigit(tokens[i]) || tokens[i] == '.')) {
                        sb.append(tokens[i++]);
                    }
                    i--;
                    values.push(Double.parseDouble(sb.toString()));
                } else if (tokens[i] == '(') {
                    operators.push(tokens[i]);
                } else if (tokens[i] == ')') {
                    while (operators.peek() != '(')
                        values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                    operators.pop();
                } else {
                    while (!operators.empty() && hasPrecedence(tokens[i], operators.peek()))
                        values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                    operators.push(tokens[i]);
                }
            } else {
                if (readingFirstPart) {
                    nonMathPart1.append(tokens[i]);
                } else {
                    nonMathPart2.append(tokens[i]);
                }
            }
        }

        while (!operators.empty())
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));

        // 使用String.format确保输出结果为小数形式，并去除末尾多余的零
        return nonMathPart1.toString() + " " + trimTrailingZeros(String.format("%.2f", values.pop())) + " " + nonMathPart2.toString();
    }

    private static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')')
            return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'))
            return false;
        else
            return true;
    }

    private static double applyOperator(char operator, double b, double a) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new UnsupportedOperationException("Cannot divide by zero");
                return a / b;
        }
        return 0;
    }

    // 去除小数部分末尾多余的零
    private static String trimTrailingZeros(String value) {
        if (!value.contains(".")) {
            return value; // 如果不含小数点，直接返回
        }
        return value.replaceAll("0*$", "").replaceAll("\\.$", ""); // 去除末尾多余的零和小数点
    }

}
