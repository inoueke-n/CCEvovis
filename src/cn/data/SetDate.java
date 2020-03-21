package cn.data;

public class SetDate {
    private int y;
    private int m;
    private int d;
    // --- コンストラクタ
    public SetDate( int y, int m, int d ) {
        this.y = y; this.m = m; this.d = d;
    }
    // --- メンバ変数の日付文字列を返す
    public String getDate() {
    	String y_s = null;
    	String m_s = null;
    	String d_s = null;

    	y_s = String.valueOf(y);

    	if(m < 10) {
    		m_s = "0" + String.valueOf(m);
    	}else {
    		m_s = String.valueOf(m);
    	}

    	if(d < 10) {
    		d_s = "0" + String.valueOf(d);
    	}else {
    		d_s = String.valueOf(d);
    	}


        return y_s +  m_s  + d_s;
    }
    // --- yがうるう年か調べる
    private boolean isLeapYear() {
        if( y % 4 == 0 && y % 100 != 0 || y % 400 == 0 ) return true;
        else return false;
    }
    // --- メンバ変数をn日後の日付で更新する
    public void addDate( int n ) {
        int days[][] =
        { { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }
        , { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 } };
        int nday[] = new int[12];

        if( n == 0 ) return;
        nday = isLeapYear() ? days[1] : days[0];
        d += n;
        if( n > 0 ) { //将来日付 dが当月内になるまで日数を引いてはmを進める
            while( d > nday[m-1] ) {
                d -= nday[m-1];
                if( ++m > 12 ) {
                    ++y; m = 1;
                    nday = isLeapYear() ? days[1] : days[0];
                }
            }
        } else { //過去日付 dが正の日付になるまで日数を足してはmを戻す
            while( d <= 0 ) {
                d += nday[m-1];
                if( --m < 1 ) {
                    --y; m = 12;
                    nday = isLeapYear() ? days[1] : days[0];
                }
            }
        }
    }
}
