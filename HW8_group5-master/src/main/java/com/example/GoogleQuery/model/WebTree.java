package com.example.GoogleQuery.model;
import java.util.ArrayList;

/**
 * WebTree - 網站樹
 * 用於建立網站之間的連結關係，並計算深度權重
*/
public class WebTree {
    
    private WebNode root;  // 根節點

    /**
     * 建構子（從 WebPage 建立）
     * @param rootPage 根節點的 WebPage
     */
    public WebTree(WebPage rootPage) {
        this.root = new WebNode(rootPage);
    }

    /**
     * 建構子（從 WebNode 建立）
     * @param rootNode 根節點
     */
    public WebTree(WebNode rootNode) {
        this.root = rootNode;
    }
    
    /**
     * 建立網站樹（遞迴）
     * 從根節點開始，抓取子網頁並建立節點
     * @param node 當前節點
     * @param currentDepth 當前深度
     * @param maxDepth 最大深度
     */
    public void buildTree(WebNode node, int currentDepth, int maxDepth) {
        if (node == null || currentDepth >= maxDepth) {
            return;
        }
        
        WebPage currentPage = node.getWebPage();
        
        // 取得當前網頁的所有對外連結
        ArrayList<String> links = currentPage.getLinks();
        
        // 限制每層最多 5 個子節點（避免樹太大）
        int childCount = 0;
        int maxChildren = 5;
        
        for (String link : links) {
            if (childCount >= maxChildren) {
                break;
            }
            
            try {
                // 建立子網頁
                WebPage childPage = new WebPage(link, extractPageName(link));
                WebNode childNode = new WebNode(childPage);
                
                // 新增子節點
                node.addChild(childNode);
                childCount++;
                
                // 遞迴建立子樹
                buildTree(childNode, currentDepth + 1, maxDepth);
                
            } catch (Exception e) {
                // 忽略無法處理的連結
                System.err.println("無法建立子節點: " + link);
            }
        }
    }
    
    /**
     * 從 URL 提取網頁名稱
     * @param url 網頁 URL
     * @return 網頁名稱
     */
    private String extractPageName(String url) {
        if (url == null || url.isEmpty()) {
            return "Unknown";
        }
        
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }
    
    /**
     * 設定後序分數（Post-Order Traversal）
     * 從葉節點開始，向上累加分數，並套用深度權重
     * @param keywords 關鍵字列表
     */
    public void setPostOrderScore(ArrayList<Keyword> keywords) {
        setPostOrderScore(root, keywords, 1.0);
    }
    
    /**
     * 設定後序分數（遞迴實作）
     * @param startNode 起始節點
     * @param keywords 關鍵字列表
     * @param weight 權重
     */
    private void setPostOrderScore(WebNode startNode, ArrayList<Keyword> keywords, double weight) {
        if (startNode == null) {
            return;
        }
        
        // 計算當前節點的分數
        startNode.setNodeScore(keywords);
        
        // 計算深度權重
        int depth = startNode.getDepth();
        double depthWeight = 1.0 / (1.0 + depth * 0.1);
        
        // 套用權重
        double weightedScore = startNode.getNodeScore() * depthWeight * weight;
        startNode.setNodeScore(weightedScore);
        
        // 遞迴處理所有子節點
        for (WebNode child : startNode.getChildren()) {
            setPostOrderScore(child, keywords, weight * 0.9); // 子節點權重遞減
        }
        
        // 累加所有子節點的分數
        double childrenScoreSum = 0.0;
        for (WebNode child : startNode.getChildren()) {
            childrenScoreSum += child.getNodeScore();
        }
        
        // 更新當前節點分數（自己的分數 + 子節點分數總和）
        startNode.setNodeScore(weightedScore + childrenScoreSum * 0.5);
    }
    
    /**
     * 歐拉遍歷列印樹結構
     */
    public void eulerPrintTree() {
        System.out.println("\n=== 網站樹結構 ===");
        eulerPrintTree(root, "", true);
    }
    
    /**
     * 歐拉遍歷列印樹結構（遞迴實作）
     * @param node 當前節點
     * @param prefix 前綴字串
     * @param isLast 是否為最後一個子節點
     */
    private void eulerPrintTree(WebNode node, String prefix, boolean isLast) {
        if (node == null) {
            return;
        }
        
        // 列印當前節點
        System.out.print(prefix);
        System.out.print(isLast ? "└── " : "├── ");
        System.out.printf("%s (分數: %.2f, 深度: %d)\n", 
            node.getWebPage().getName(), 
            node.getNodeScore(),
            node.getDepth()
        );
        
        // 遞迴列印子節點
        ArrayList<WebNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            boolean isLastChild = (i == children.size() - 1);
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            eulerPrintTree(children.get(i), newPrefix, isLastChild);
        }
    }
    
    /**
     * 取得根節點
     * @return 根節點
     */
    public WebNode getRoot() {
        return root;
    }
    
    /**
     * 取得樹的高度
     * @return 樹的高度
     */
    public int getHeight() {
        return getHeight(root);
    }
    
    /**
     * 取得樹的高度（遞迴實作）
     * @param node 當前節點
     * @return 高度
     */
    private int getHeight(WebNode node) {
        if (node == null || node.isLeaf()) {
            return 0;
        }
        
        int maxChildHeight = 0;
        for (WebNode child : node.getChildren()) {
            int childHeight = getHeight(child);
            if (childHeight > maxChildHeight) {
                maxChildHeight = childHeight;
            }
        }
        
        return maxChildHeight + 1;
    }
    
    /**
     * 取得樹中節點總數
     * @return 節點總數
     */
    public int getNodeCount() {
        return getNodeCount(root);
    }
    
    /**
     * 取得節點總數（遞迴實作）
     * @param node 當前節點
     * @return 節點數
     */
    private int getNodeCount(WebNode node) {
        if (node == null) {
            return 0;
        }
        
        int count = 1; // 當前節點
        for (WebNode child : node.getChildren()) {
            count += getNodeCount(child);
        }
        
        return count;
    }
    
    /**
     * 取得樹的總分數（根節點分數）
     * @return 總分數
     */
    public double getTotalScore() {
        return root != null ? root.getNodeScore() : 0.0;
    }
    
    /**
     * 重複字串（用於格式化輸出）
     * @param str 要重複的字串
     * @param times 重複次數
     * @return 重複後的字串
     */
    public String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return String.format(
            "WebTree{root='%s', height=%d, nodes=%d, totalScore=%.2f}",
            root.getWebPage().getName(),
            getHeight(),
            getNodeCount(),
            getTotalScore()
        );
    }
}

