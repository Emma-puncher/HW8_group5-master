package com.example.GoogleQuery.model;

import java.util.ArrayList;

/**
 * WebNode - 網站節點
 * 用於建立網站樹結構，每個節點代表一個網頁
 */
public class WebNode {
    
    private WebNode parent;              // 父節點
    private ArrayList<WebNode> children; // 子節點列表
    private WebPage webPage;             // 網頁資料
    private double nodeScore;            // 節點分數
    
    /**
     * 建構子
     * @param webPage 網頁資料
     */
    public WebNode(WebPage webPage) {
        this.webPage = webPage;
        this.parent = null;
        this.children = new ArrayList<>();
        this.nodeScore = 0.0;
    }
    
    /**
     * 設定節點分數（基於關鍵字）
     * @param keywords 關鍵字列表
     */
    public void setNodeScore(ArrayList<Keyword> keywords) {
        if (webPage != null) {
            webPage.setScore(keywords);
            this.nodeScore = webPage.getScore();
        }
    }
    
    /**
     * 新增子節點
     * @param child 子節點
     */
    public void addChild(WebNode child) {
        if (child != null) {
            children.add(child);
            child.parent = this;
        }
    }
    
    /**
     * 檢查是否為最後一個子節點
     * @return true 如果是最後一個子節點
     */
    public boolean isTheLastChild() {
        if (parent == null) {
            return true;
        }
        
        ArrayList<WebNode> siblings = parent.children;
        return siblings.indexOf(this) == siblings.size() - 1;
    }
    
    /**
     * 取得節點深度（從根節點算起）
     * @return 深度（根節點為 0）
     */
    public int getDepth() {
        int depth = 0;
        WebNode current = this;
        
        while (current.parent != null) {
            depth++;
            current = current.parent;
        }
        
        return depth;
    }
    
    /**
     * 檢查是否為根節點
     * @return true 如果是根節點
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    /**
     * 檢查是否為葉節點（沒有子節點）
     * @return true 如果是葉節點
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    // ========== Getters ==========
    
    public WebNode getParent() {
        return parent;
    }
    
    public ArrayList<WebNode> getChildren() {
        return new ArrayList<>(children);
    }
    
    public WebPage getWebPage() {
        return webPage;
    }
    
    public double getNodeScore() {
        return nodeScore;
    }
    
    public void setNodeScore(double nodeScore) {
        this.nodeScore = nodeScore;
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return String.format(
            "WebNode{page='%s', score=%.2f, depth=%d, children=%d}",
            webPage.getName(), nodeScore, getDepth(), children.size()
        );
    }
}


