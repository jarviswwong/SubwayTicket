package cn.mcavoy.www.subwayticket.Pinyin;

import java.util.Comparator;

import cn.mcavoy.www.subwayticket.subwayListModel.StationModel;

public class PinyinComparator implements Comparator<StationModel.StationsEntity> {
    @Override
    public int compare(StationModel.StationsEntity lhs, StationModel.StationsEntity rhs) {
        return lhs.getSortLetters().compareTo(rhs.getSortLetters());
    }
}
