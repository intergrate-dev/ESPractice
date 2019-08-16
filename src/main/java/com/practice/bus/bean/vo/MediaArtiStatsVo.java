package com.practice.bus.bean.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @author yuan-pc
 */
public class MediaArtiStatsVo implements Serializable {

    private String id;
    private String sourceName;
    private List<Stats> stats;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public List<Stats> getStats() {
        return stats;
    }

    public void setStats(List<Stats> stats) {
        this.stats = stats;
    }

    public static class Stats {
        private String weekday;
        private Data data;

        public String getWeekday() {
            return weekday;
        }

        public void setWeekday(String weekday) {
            this.weekday = weekday;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public static class Data {
            private int publish;
            private int scan;
            private int like;

            public int getPublish() {
                return publish;
            }

            public void setPublish(int publish) {
                this.publish = publish;
            }

            public int getScan() {
                return scan;
            }

            public void setScan(int scan) {
                this.scan = scan;
            }

            public int getLike() {
                return like;
            }

            public void setLike(int like) {
                this.like = like;
            }
        }
    }
}
