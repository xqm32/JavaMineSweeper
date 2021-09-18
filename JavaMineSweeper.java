import java.util.Map;
import static java.util.Map.entry;
import java.util.Random;
import java.util.Scanner;

// TODO 需要优化：异常处理
class MineGrid {
    public static class InvalidFlag extends RuntimeException {
        @Override
        public String getMessage() {
            return "Invalid Flag";
        }
    }

    // 用于 gridMine，标注 grid 是否有雷
    public enum Mine {
        BLANK, MINE
    }

    // 用于 gridFlag，标注 grid 的备注
    // 这里我们设定若为可见时，则无法进行标记（也没有必要进行标记）
    public enum Flag {
        INVALID, INVISABLE, VISABLE, IS_MINE, IS_QUERY, IS_SAFE
    }

    private int length;
    private int width;
    private int mines;

    // 可见的格子数量，用于判断游戏是否胜利
    private int visableGrids = 0;

    private boolean inited;
    private boolean gameOver;

    private Mine[][] gridMine;
    private Flag[][] gridFlag;
    private int[][] gridNumber;

    private static final Map<String, Flag> flags = Map.ofEntries(entry("NOFLAG", Flag.INVISABLE),
            entry("MINE", Flag.IS_MINE), entry("QUERY", Flag.IS_QUERY), entry("SAFE", Flag.IS_SAFE));

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
                // TODO here
                // 成品应设为 Flag.INVISABLE
                gridFlag[i][j] = Flag.INVISABLE;
                // gridFlag[i][j] = Flag.VISABLE;
                gridNumber[i][j] = 0;
            }
        }

        inited = false;
        gameOver = false;
    }

    private int initGrid() {
        int setMineX, setMineY;
        Random rand = new Random();

        for (int i = 0; i < mines; ++i) {
            // 随机生成置放雷的坐标，若已存在，则重新放置
            // 此仅为置放雷的算法的一种，实际上可以使用 *洗牌算法* 置放雷
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

    // 对于有雷的格子使用，作用是将其周围的 gridNumber（附近雷数）增一
    private int setNumber(int x, int y) {
        // 以下四行是检测越界的
        int startX = x - 1 < 0 ? x : x - 1;
        int startY = y - 1 < 0 ? y : y - 1;
        int endX = x + 1 < length ? x + 1 : x;
        int endY = y + 1 < width ? y + 1 : y;
        for (int i = startX; i <= endX; ++i) {
            for (int j = startY; j <= endY; ++j) {
                // ! 注意：这里的这个特性在 searchSafe() 方法中已经被用到，
                // ! 请勿再进行更改
                // 由于 gridNumber 和 gridMine 是分开的，这里不需要跳过雷格
                // if (i == x && j == y)
                // continue;
                gridNumber[i][j] += 1;
            }
        }
        return 0;
    }

    private void searchSafe(int x, int y) {
        /*
         ! 注意：此处用了 setNumber() 方法中的一个特性，也就是有 雷 处的 gridNumber 不为零
         ! 但实际上，雷 的周围的 girdNumber 本就不为零，即使不利用此特性，理论上也不会出现问题
         ! 但出于规范考虑，还是请勿修改 setNumber() 这一方法
         */
        // 越界检测
        // 由于这里有越界检测了，此后不再进行越界检测
        if (x < 0 || y < 0 || x >= length || y >= width)
            return;
        // 达到边界检测
        if (gridFlag[x][y] == Flag.VISABLE)
            return;

        gridFlag[x][y] = Flag.VISABLE;
        visableGrids += 1;

        // 此处与上一行顺序不可以调换，因为需要显示数字
        if (gridNumber[x][y] != 0)
            return;

        for (int i = x - 1; i <= x + 1; ++i) {
            for (int j = y - 1; j <= y + 1; ++j) {
                // 此处无需进行对原坐标的优化，因为此处将会跳过已经 Flag.VISABLE 的格子
                searchSafe(i, j);
            }
        }
    }

    private char toChar(Mine mine, Flag flag, int number) {
        if (flag == Flag.VISABLE) {
            if (mine == Mine.MINE)
                return '*';
            else
                return (char) (number + '0');
        } else {
            // ? 为什么这里不需要 Flag.FLAG 呢？
            // 参见：https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
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

    private Flag toFlag(String mark) {
        if (flags.containsKey(mark)) {
            return flags.get(mark);
        } else {
            return Flag.INVALID;
        }
    }

    public String toString() {
        return toString(true);
    }

    // 将格子转化为 String，方便输出
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

    public void clickGrid(int x, int y) {
        // 如果是第一次点击，不应是雷
        // 若是雷，则重新生成
        if (!inited) {
            do {
                initGrid();
            } while (gridMine[x][y] == Mine.MINE);
            inited = true;
        }
        // 非初始化，且点击的格子含雷
        if (gridMine[x][y] == Mine.MINE) {
            gameOver = true;
            return;
        }
        searchSafe(x, y);
        return;
    }

    // 若可见的格子数量与雷数量恰为总格数，则判定胜利
    public boolean isWin() {
        if (visableGrids + mines == length * width) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOver() {
        return gameOver;
    }

    public void markGrid(int markX, int markY, String mark) {
        gridFlag[markX][markY] = toFlag(mark);
    }

    public void setAllGridVisable() {
        for (int i = 0; i < length; ++i) {
            for (int j = 0; j < width; ++j) {
                gridFlag[i][j] = Flag.VISABLE;
            }
        }
    }
}

// TODO 写的不好，需要重构
// 通过指令类完成输入输出
class Instruction {
    // 实际上枚举类型是可以拥有更加丰富的内容的
    // 参考：https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
    public enum Type {
        NO_COMMAND, INVALID_COMMAND, NUMBER_EXPECTED, MARK_EXPECTED, COORDINATE, CLICK, MARK, QUIT
    }

    private Type type;
    private String mark;

    private int[] coordinate = new int[2];
    // 这是一个常 Map，包含了字符串对 Instruction.Type 的映射
    private static final Map<String, Type> instructions = Map.ofEntries(entry("CLICK", Type.CLICK),
            entry("MARK", Type.MARK), entry("QUIT", Type.QUIT));

    public Instruction() {
        type = Type.NO_COMMAND;
    }

    // 不应当有默认 System.out.in 的打开再读取
    // 否则将会无法进行持续读取
    public void read(Scanner scan) {
        String[] instruction;

        if (scan.hasNextLine()) {
            instruction = scan.nextLine().strip().split(" ");
            String insKey = instruction[0];

            if (instructions.containsKey(insKey)) {
                type = instructions.get(insKey);
                switch (type) {
                    // TODO 需要优化为异常处理
                    case CLICK:
                        type = readCoordinate(instruction);
                        return;
                    case MARK:
                        type = readCoordinate(instruction);
                        type = readMark(instruction);
                        return;
                    case QUIT:
                        return;
                    default:
                        type = Type.INVALID_COMMAND;
                        return;
                }
            }
        } else {
            type = Type.NO_COMMAND;
            return;
        }
    }

    private Type readCoordinate(String[] instruction) {
        if (instruction.length < 3) {
            return Type.NUMBER_EXPECTED;
        }
        try {
            // 坐标需要更换顺序，以符合 X 轴、 Y 轴
            coordinate[0] = Integer.parseInt(instruction[2]);
            coordinate[1] = Integer.parseInt(instruction[1]);
        } catch (NumberFormatException e) {
            return Type.NUMBER_EXPECTED;
        }
        // TODO 需要优化代码逻辑
        // 这里已经在 read() 方法中判断过 instruction[0] 是包含在 instructions 中的
        return instructions.get(instruction[0]);
    }

    private Type readMark(String[] instruction) {
        if (instruction.length < 4) {
            return Type.MARK_EXPECTED;
        }
        mark = instruction[3];
        return Type.MARK;
    }

    public int[] getCoordinate() {
        return coordinate;
    }

    public Type getType() {
        return type;
    }

    public String getMark() {
        return mark;
    }
}

public class JavaMineSweeper {
    static MineGrid mineGrid;

    static void game(Scanner scan) {
        Instruction ins = new Instruction();
        Instruction.Type insType;
        int[] coordinate;

        while (!mineGrid.isOver()) {
            System.out.print(mineGrid.toString());
            System.out.print("Input Command: ");

            ins.read(scan);
            insType = ins.getType();

            // 这里不用 switch，以方便 break
            if (insType == Instruction.Type.CLICK) {
                int clickX, clickY;
                coordinate = ins.getCoordinate();
                clickX = coordinate[0];
                clickY = coordinate[1];
                mineGrid.clickGrid(clickX, clickY);
            } else if (insType == Instruction.Type.MARK) {
                int markX, markY;
                String mark;
                coordinate = ins.getCoordinate();
                markX = coordinate[0];
                markY = coordinate[1];
                mark = ins.getMark();
                mineGrid.markGrid(markX, markY, mark);
            } else if (insType == Instruction.Type.QUIT) {
                System.out.println("You Quit");
                return;
            }

            if (mineGrid.isWin()) {
                System.out.println("You Win");
                return;
            }
        }
        return;
    }

    public static void main(String[] args) {
        mineGrid = new MineGrid(10, 10, 10);
        Scanner scan = new Scanner(System.in);
        game(scan);
        mineGrid.setAllGridVisable();
        System.out.print(mineGrid.toString());
        scan.close();
    }
}