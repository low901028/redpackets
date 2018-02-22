/**
 * Created by jdd on 2018/1/26.
 */
public class LeftMoneyPackage {
    private volatile Integer remainSize;
    private volatile Double remainMoney;
    private volatile Double minMoney;

    public Double getMinMoney() {
        return minMoney;
    }

    public void setMinMoney(Double minMoney) {
        this.minMoney = minMoney;
    }

    public Integer getRemainSize() {
        return remainSize;
    }

    public void setRemainSize(Integer remainSize) {
        this.remainSize = remainSize;
    }

    public Double getRemainMoney() {
        return remainMoney;
    }

    public void setRemainMoney(Double remainMoney) {
        this.remainMoney = remainMoney;
    }
}
