package com.whe.redis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页辅助类：对分页的基本数据进行一个简单的封装
 * 用来传递分页参数和查询参数params
 * Created by wang hongen on 2016/10/19.
 */
public class Page<T> {
    private int pageNo = 1;                            //页码，默认是第一页
    private int pageSize = ServerConstant.PAGE_NUM;    //每页显示的记录数，
    private long totalRecord;                        //总记录数
    private int totalPage;                            //总页数
    private T results;                        //对应的当前页记录
    private Map<String, Object> params = new HashMap<>();        //其他的参数我们把它分装成一个Map对象
    /**
     * 当前页的分页样式
     */
    private List<String> pageView;

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(long totalRecord) {
        this.totalRecord = totalRecord;
        //在设置总页数的时候计算出对应的总页数，在下面的三目运算中加法拥有更高的优先级，所以最后可以不加括号。
        int totalPage = (int) (totalRecord % pageSize == 0 ? totalRecord / pageSize : totalRecord / pageSize + 1);
        this.setTotalPage(totalPage);
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }


    public void setPageView(List<String> pageView) {
        this.pageView = pageView;
    }

    public List<String> getPageView() {
        return pageView;
    }

    /**
     * ajax分页显示
     * boorStrap分页样式
     *
     * @param url    连接
     * @param params 后面参数
     */
    public void pageViewAjax(String url, String params) {
        pageView = new ArrayList<>();
        if (totalPage <= 1) {
            return;
        }
        pageView.add("<ul class=\"pagination\">");
        if (this.pageNo != 1) {
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=1" + params + "',this)\">首页</a></li>");
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (this.pageNo - 1) + params + "',this)\">上一页</a></li>");
        }

        if (this.getTotalPage() <= 10) {
            for (int i = 0; i < this.getTotalPage(); i++) {
                if ((i + 1) == this.pageNo) {
                    pageView.add("<li class=\"active\"><a href=\"javascript:void(0)\">" + this.pageNo + "</a></li>");
                    i = i + 1;
                    if (this.pageNo == this.getTotalPage()) break;
                }
                pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (i + 1) + params + "',this)\">" + (i + 1) + "</a></li>");
            }
        } else if (this.getTotalPage() <= 20) {
            //没有把...加上
            int left;
            int right;
            if (this.pageNo < 5) {
                left = this.pageNo - 1;
                right = 10 - left - 1;
            } else if (this.getTotalPage() - this.pageNo < 5) {
                right = this.getTotalPage() - this.pageNo;
                left = 10 - 1 - right;
            } else {
                right = 5;
                left = 4;
            }
            int tmp = this.pageNo - left;
            for (int i = tmp; i < tmp + 10; i++) {
                if (i == this.pageNo) {
                    pageView.add("<li class=\"active\"><a href=\"javascript:void(0)\">" + this.pageNo + "</a></li>");
                    i = i + 1;
                    if (this.pageNo == this.getTotalPage()) break;
                }
                pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (i) + params + "',this)\">" + (i) + "</a></li>");
            }

        } else if (this.pageNo < 6) {
            for (int i = 1; i <= 8; i++) {
                if (i == this.pageNo) {
                    pageView.add("<li class=\"active\"><a href=\"javascript:void(0)\">" + this.pageNo + "</a></li>");
                    continue;
                }
                pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (i) + params + "',this)\">" + (i) + "</a></li>");
            }
            pageView.add("<li class=\"disabled\"><a href=\"javascript:void(0)\">...</a></li>");
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (this.getTotalPage() - 1) + params + "',this)\">" + (this.getTotalPage() - 1) + "</a></li>");
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (this.getTotalPage()) + params + "',this)\">" + (this.getTotalPage()) + "</a></li>");
        } else if (this.pageNo > this.getTotalPage() - 6) {
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (1) + params + "',this)\">" + (1) + "</a></li>");
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (2) + params + "',this)\">" + (2) + "</a></li>");
            pageView.add("<li class=\"disabled\"><a href=\"javascript:void(0)\">...</a></li>");
            for (int i = this.getTotalPage() - 8; i < this.getTotalPage(); i++) {
                if (i + 1 == this.pageNo) {
                    pageView.add("<li class=\"active\"><a href=\"javascript:void(0)\">" + this.pageNo + "</a></li>");
                    i = i + 1;
                    if (this.pageNo == this.getTotalPage()) break;
                }
                pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (i + 1) + params + "',this)\">" + (i + 1) + "</a></li>");
            }
        } else {
            for (int i = 4; i > -4; i--) {
                if (this.pageNo - i == this.pageNo) {
                    pageView.add("<li class=\"active\"><a href=\"javascript:void(0)\">" + this.pageNo + "</a></li>");
                    continue;
                }
                pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (this.pageNo - i) + params + "',this)\">" + (this.pageNo - i) + "</a></li>");
            }

            pageView.add("<li class=\"disabled\"><a href=\"javascript:void(0)\">...</a></li>");
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (this.getTotalPage() - 1) + params + "',this)\">" + (this.getTotalPage() - 1) + "</a></li>");
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (this.getTotalPage()) + params + "',this)\">" + (this.getTotalPage()) + "</a></li>");
        }
        if (this.pageNo != this.getTotalPage()) {
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (this.pageNo + 1) + params + "',this)\"><font size=2>下一页</font></a></li>");
            pageView.add("<li><a href=\"javascript:void(0);\" onclick=\"pageViewAjax('" + url + "?pageNo=" + (this.getTotalPage()) + params + "',this)\"><font size=2>尾页</font></a></li>");
        }
        pageView.add("</ul>");
    }

}