import java.util.Map;
import static java.util.Map.entry;
import java.util.Random;
import java.util.Scanner;

public class JavaMineSweeper {
    public static class MineGrid {
        // 用于 gridMine，标注 grid 是否有雷
        enum Mine {
            BLANK, MINE
        }

        // 用于 gridFlag，标注 grid 的备注
        // 这里我们设定若为可见时，则无法进行标记（也没有必要进行标记）
        enum Flag {
            INVISABLE, VISABLE, IS_MINE, IS_QUERY, IS_SAFE
        }

        public int length;
        public int width;
        public int mines;

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
                    gridFlag[i][j] = Flag.VISABLE;
                    gridNumber[i][j] = 0;
                }
            }

            initGrid();
        }

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
                    return 'X';
                else if (number == 0)
                    return ' ';
                else
                    return (char) (number + '0');
            } else {
                // TODO 完善关于 flag 的字符转化
                switch (flag) {
                    case INVISABLE:
                        return ' ';
                    default:
                        return ' ';
                }
            }
        }

        public String toString() {
            String ret = new String();
            char[][] gridString = new char[length][width];

            for (int i = 0; i < length; ++i) {
                for (int j = 0; j < width; ++j) {
                    gridString[i][j] = toChar(gridMine[i][j], gridFlag[i][j], gridNumber[i][j]);
                }
            }

            // TODO 需要优化，这里不断地为 gridString 分配内存，时间复杂度较高
            for (int i = 0; i < length; ++i) {
                String temp = new String(gridString[i]);
                ret = ret.concat(temp + "\n");
            }

            return ret;
        }
    }

    // 通过指令类完成输入输出
    public static class Instruction {
        enum Type {
            NONE, CLICK, COORDINATE, MARK, RESTART, ERROR
        }

        public Type type;
        public int coordinate;
        public static final Map<String, Type> instructions = Map.ofEntries(entry("CLICK", Type.CLICK),
                entry("RESTART", Type.RESTART));

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

    }

    public static void main(String[] args) {
        MineGrid mineGrid = new MineGrid(10, 10, 10);
        System.out.println(mineGrid.toString());

        // Instruction ins = new Instruction();
        // ins.read();
    }
}