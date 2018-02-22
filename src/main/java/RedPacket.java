import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by jdd on 2018/1/25.
 */
public class RedPacket {
    private static final double NV_MAGICCONST = 4 * Math.exp(-0.5)/ Math.sqrt(2.0);

    /**
     * 正态分布随机数[截断正态分布]
     * @param mean 平均数
     * @param sgima 标准差
     * @return
     */
    private  static double normalVariate(double mean , double sgima){
        Random random = new Random();
        double z = 0.0;
        while (true) {
            double u1 = random.nextDouble();
            double u2 = 1 - random.nextDouble();
             z = NV_MAGICCONST * (u1 - 0.5) / u2;
            double zz = z * z / 4.0;
            if (zz <= -Math.log(u2)) {
                break;
            }
        }
        return (mean + z * sgima);
    }

    /**
     *  基于截尾正态分布版红包
     截尾正态分布：将满足正态分布的数据剔除负值之后剩余的部分限制在一个区间内得到的新的概率分布
     1、得到红包平均值、标准差[采用平均值/2 替代真实的样本集的标准差]
     2、其次只需要计算前n-1个红包对应的随机额度
        a、每个红包分配权重 / 分配的总权重和 * 总发放额度 即为当前发放红包的额度  每个分配红包的权重满足正态分布
        b、在记录下来已发放额度红包的额度【用于记录最后一个红包的额度】
        c、循环前面的a和b的操作
     3、最后一个红包=总发放额度 - 前n-1个红包的总额度
     4、返回所有分配的红包
     * @param money 派发总额度
     * @param n     派发红包个数
     * @param minMoney 红包最小额度  现金红包不需要指定
     * @param maxMoney 红包最大额度  防止出现一个人占据整个红包总额
     * @return
     */
    private static List<BigDecimal> redPacketsRandom_Sigma(Integer money, int n , double minMoney, double maxMoney){
        // 生成红包
        List<BigDecimal> redPackets = new ArrayList<BigDecimal>();
        try {
            BigDecimal mean = BigDecimal.valueOf(money / n); // 平均值
            System.out.println("mean === " + mean);
            BigDecimal sigma = BigDecimalUtils.safeDivide(mean , BigDecimal.valueOf(2.0)); // 标准差
            System.out.println("sigma === " + sigma);
            List<BigDecimal> divide_table = new ArrayList<BigDecimal>();  // 每个红包额度权重
            double sum_ = 0.0;

            while(divide_table.size() < n){  // 需要分配额度红包 必须是 < 预分配红包的总个数
                double random_float = normalVariate(mean.doubleValue(),sigma.doubleValue());

                if(BigDecimalUtils.is(random_float).gte(sigma) && BigDecimalUtils.is(random_float).lte(2.0 * sigma.doubleValue())) {
                    System.out.println("random_float === " + random_float);
                    divide_table.add(BigDecimal.valueOf(random_float));
                    sum_ += random_float;
                }
            }

            double cur_sum = 0.0;

            for(int i=0; i<n-1;i++){
                double cur = Math.round((divide_table.get(i).doubleValue() * money / sum_));
                System.out.println("cur = " + cur);
                if(BigDecimalUtils.is(cur).lt(minMoney)){
                    System.out.println("default cur = " + minMoney);
                    cur = minMoney;
                }
                /*else if(BigDecimalUtils.is(cur).gt(maxMoney)){
                    System.out.println("cur = " + maxMoney);
                    cur = maxMoney;
                }*/

                redPackets.add(BigDecimal.valueOf(cur));
                cur_sum += cur;
            }

            //BigDecimal remain_money = BigDecimalUtils.safeDivide(money, cur_sum);  // 保证最小额度不低于100
            BigDecimal remain_money = BigDecimalUtils.safeSubtract(money, cur_sum);

            System.out.println(" 最后一个红包额度 = " + remain_money);

            /*if(BigDecimalUtils.is(remain_money).lt(minMoney)) {
                remain_money = BigDecimal.valueOf(minMoney);
            }*/


            System.out.println(cur_sum + remain_money.doubleValue());

            redPackets.add(remain_money);

        }catch (Exception exp){
            System.out.println("生成红包抛出异常...");
        }

        return  redPackets;
    }

    /**
     * 随机红包
     * 原理： 第一个红包  额度 = [最小额度,总派发额度/总红包个数 * 2]内任意一个随机数  即为对应红包的额度
     *        第二个红包  额度 = [最小额度,(总派发额度-第一个红包的额度)/(总红包个数-1) * 2]内任意一个随机数  即为对应红包的额度
     *        第三个红包  额度 = [最小额度,(总派发额度-前2个红包的额度)/(总红包个数-2) * 2]内任意一个随机数  即为对应红包的额度
     *        如此循环直至第n-1个红包 额度 = [最小额度,(总派发额度-前n-2个红包的额度)/(总红包个数-(n-2)) * 2]内任意一个随机数  即为对应红包的额度
     *        第n个红包 额度 = 总额度 - 前n-1个红包的派发额度
     *        需要指定当前红包的个数、红包的总额度、红包最小额度
     * @param _leftMoneyPackage
     * @param unit
     * @return
     */
    public static double getRandomMoney(LeftMoneyPackage _leftMoneyPackage, int unit){
        int _leftSize = _leftMoneyPackage.getRemainSize(); // 剩余红包个数
        double _leftMoney = _leftMoneyPackage.getRemainMoney(); // 剩余的额度
        double _minMoney = _leftMoneyPackage.getMinMoney(); // 最小红包额度

        System.out.println("红包-剩余额度 = " + _leftMoney + " 剩余红包个数= "+ _leftSize);
        if(_leftSize == 1){
            _leftMoneyPackage.setRemainSize(--_leftSize);

            return  (double)Math.round(_leftMoney * unit) / unit;
        }
        Random random = new Random();
        double max = _leftMoney / _leftSize * 2;
        double money = random.nextDouble() * max;

        money = BigDecimalUtils.is(money).lte(_minMoney) ? _minMoney : money;
        money = Math.floor(money * unit) / unit;
        _leftMoneyPackage.setRemainSize(--_leftSize);
        _leftMoneyPackage.setRemainMoney(BigDecimalUtils.safeSubtract(_leftMoney, money).doubleValue());

        return money;

    }

    public static void main(String[] args){
        System.out.println(redPacketsRandom_Sigma(10000, 5, 100.0,2000.0));

        LeftMoneyPackage moneyPackage = new LeftMoneyPackage();
        moneyPackage.setRemainMoney(10000.0);
        moneyPackage.setRemainSize(5);
        moneyPackage.setMinMoney(100.0);

        int i = 0;
        while (i<5){
            System.out.println("红包-" + i + " 额度= "+ getRandomMoney(moneyPackage,1));
            i++;
        }

        //NormalDistribution normalDistributioin = new NormalDistribution(3300/25,3300/25/2);
        //System.out.println(normalDistributioin.cumulativeProbability(1.0));
    }
}
