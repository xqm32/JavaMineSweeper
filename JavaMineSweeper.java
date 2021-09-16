import java.util.Map;
import static java.util.Map.entry;
import java.util.Random;
import java.util.Scanner;

class MineGrid {
    // 用于 gridMine，标注 grid 是否有雷
    public enum Mine {
        BLANK, MINE
    }

    // 用于 gridFlag，标注 grid 的备注
    // 这里我们设定若为可见时，则无法进行标记（也没有必要进行标记）
    public enum Flag {
        INVISABLE, VISABLE, IS_MINE, IS_QUERY, IS_SAFE
    }

    private int length;
    private int width;
    private int mines;

    private Mine[][] gridMine;
    private Flag[][] gridFlag;
    private int[][] gridNumber;

    public MineGrid(int startLength, int startWidth, int startMines) {
        length = startLength;
        width = startWidth;
        mines = startMines;
        gridMine = new Mine[length][width];
        gridFlag = new Flag[length][width];
        gridNumber = new int[length][width];

        // 将每个 grid 都初始化为 *无雷*、*不可见*
        for (int i = 0; i < length; ++i) {
            for (int j = 0; j < width; ++j) {
                gridMine[i][j] = Mine.BLANK;
                // 成品应设为 Flag.INVISABLE
                // gridFlag[i][j] = Flag.INVISABLE;
                gridFlag[i][j] = Flag.VISABLE;
                gridNumber[i][j] = 0;
            }
        }

        initGrid();
    }

    // 对于有 雷 的格子使用，作用是将其周围的 gridNumber（附近雷数）增一
    private int setNumber(int x, int y) {
        int startX = x - 1 < 0 ? x : x - 1;
        int startY = y - 1 < 0 ? y : y - 1;
        int endX = x + 1 < length ? x + 1 : x;
        int endY = y + 1 < width ? y + 1 : y;
        for (int i = startX; i <= endX; ++i) {
            for (int j = startY; j <= endY; ++j) {
                // 由于 gridNumber 和 gridMine 是分开的，这里不需要跳过 雷 格
                // if (i == x && j == y)
                // continue;
                gridNumber[i][j] += 1;
            }
        }
        return 0;
    }

    private int initGrid() {
        int setMineX, setMineY;
        Random rand = new Random();

        for (int i = 0; i < mines; ++i) {
            // 随机生成置放 雷 的坐标，若已存在，则重新放置
            // 此仅为置放 雷 的算法的一种，实际上可以使用 *洗牌算法* 置放 雷
            setMineX = rand.nextInt(length);
            setMineY = rand.nextInt(width);

            if (gridMine[setMineX][setMineY] != Mine.MINE) {
                gridMine[setMineX][setMineY] = Mine.MINE;
            } else {
                --i;
                continue;
            }

            setNumber(setMineX, setMineY);
        }

        return 0;
    }

    private char toChar(Mine mine, Flag flag, int number) {
        if (flag == Flag.VISABLE) {
            if (mine == Mine.MINE)
                return '*';
            else if (number == 0)
                return ' ';
            else
                return (char) (number + '0');
        } else {
            // ? 为什么这里不需要 Flag.FLAG 呢？
            switch (flag) {
                case INVISABLE:
                    return ' ';
                case IS_MINE:
                    return 'X';
                case IS_QUERY:
                    return '?';
                case IS_SAFE:
                    return 'O';
                default:
                    return ' ';
            }
        }
    }

    public String toString() {
        return toString(true);
    }

    public String toString(boolean useAxis) {
        StringBuilder ret = new StringBuilder();
        StringBuilder axisX = new StringBuilder("  ");
        char[][] gridString = new char[length][width];

        for (int i = 0; i < length; ++i) {
            for (int j = 0; j < width; ++j) {
                gridString[i][j] = toChar(gridMine[i][j], gridFlag[i][j], gridNumber[i][j]);
            }
        }

        if (useAxis) {
            for (int i = 0; i < length; ++i) {
                axisX.append(i + " ");
            }
            axisX.append("\n");

            ret.append(axisX.toString());
            for (int i = 0; i < length; ++i) {
                ret.append(i + " ");
                for (int j = 0; j < width; ++j) {
                    ret.append(gridString[i][j] + " ");
                }
                ret.append(i + "\n");
            }
            ret.append(axisX.toString());
        } else {
            for (int i = 0; i < length; ++i) {
                for (int j = 0; j < width; ++j) {
                    ret.append(gridString[i][j] + " ");
                }
            }
        }
        return ret.toString();
    }
}

// TODO 写的不好，需要重构
// 通过指令类完成输入输出
class Instruction {
    public enum Type {
        NONE, COORDINATE, ERROR, CLICK, MARK, RESTART
    }

    private Type type;
    private int coordinate;
    private static final Map<String, Type> instructions = Map.ofEntries(entry("CLICK", Type.CLICK),
            entry("RESTART", Type.RESTART), entry("MARK", Type.MARK));

    public Instruction() {
        type = Type.NONE;
    }

    public Instruction.Type read(Scanner scan) {
        String instruction;

        if (scan.hasNextInt()) {
            type = Type.COORDINATE;
            coordinate = scan.nextInt();
            return Type.COORDINATE;
        } else if (scan.hasNext()) {
            instruction = scan.next();
            if (instructions.containsKey(instruction)) {
                type = instructions.get(instruction);
            }
            return type;
        } else
            return Type.ERROR;
    }

    public Instruction.Type read() {
        Scanner scan = new Scanner(System.in);
        // 这里不再重写 read 函数了
        Instruction.Type ret = read(scan);
        scan.close();
        return ret;
    }

    public int getCoordinate() {
        return coordinate;
    }
}

public class JavaMineSweeper {
    static MineGrid mineGrid;

    static int game() {
        System.out.print(mineGrid.toString());
        return 0;
    }

    public static void main(String[] args) {
        mineGrid = new MineGrid(10, 10, 10);
        game();
    }
}