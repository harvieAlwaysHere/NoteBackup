## **必读经典文章**

#### **算法的框架思维**

##### **数据基本结构**

数据存储的**基本结构**只有两种，优缺点如下

* **数组**
  * 顺序存储，连续空间，索引访问
  * 插入删除元素需搬移数据保持连续，扩容需重新分配更大空间后复制数据
* **链表**
  * 链式存储，指针访问，插入删除元素易
  * 存储空间不连续无法随机访问，消耗更多空间存储指针

##### **数据结构基本操作**

数据结构的**基本操作**即**遍历和访问(增删改查)**，可分为**线性迭代**和**非线性递归**，**基本框架**如下

```java
// 数组迭代框架
void traverse(int[] arr) {
    for (int i = 0; i < arr.length; i++) {
        // 迭代访问 arr[i]
    }
}

// 链表基本结构
class ListNode {
    int val;
    ListNode next;
}

// 链表迭代框架
void traverse(ListNode head) {
    for (ListNode p = head; p != null; p = p.next) {
        // 迭代访问 p.val
    }
}

// 链表递归框架
void traverse(ListNode head) {
    // 递归访问 head.val
    traverse(head.next)
}

// 二叉树基本结构
class TreeNode {
    int val;
    TreeNode left, right;
}

// 二叉树递归框架
void traverse(TreeNode root) {
    // 前序递归访问 root.val
    traverse(root.left)
    // 中序递归访问 root.val
    traverse(root.right)
    // 后序递归访问 root.val
}

// N叉树基本结构
class TreeNode {
    int val;
    TreeNode[] children;
}

// N叉树迭代+递归框架
void traverse(TreeNode root) {
    for (TreeNode child : root.children)
        // 递归访问 root.val
        traverse(child);
}
```

##### **算法基本框架**

算法是通过**合适的数据结构解决特定问题**的方法

大部分算法技巧本质上是**树的(前中后序)遍历**，对应题目例子如下

```java
// 【后序遍历】LeetCode-124 求二叉树内最大路径和
int ans = Integer.MIN_VALUE;
int oneSideMax(TreeNode root) {
    if (root == null) return 0;
    int left = Math.max(0, oneSideMax(root.left)); 
    int right = Math.max(0, oneSideMax(root.right));
    //当前节点作为根节点 连接左右节点路径的最大值
    ans = Math.max(ans, left + right + root.val);
    //当前节点作为子节点 向上递归
    return Math.max(left, right) + root.val;
}

// 【前序遍历】LeetCode-124 根据前序和中序还原二叉树
private TreeNode buildTree(
    int[] preorder, int preStart, int preEnd,
    int[] inorder, int inStart, int inEnd){
    
    if(preStart > preEnd || inStart > inEnd) return null;

    TreeNode root = new TreeNode(preorder[preStart]);
    int inorderRootIndex = getIndex(inorder, inStart, inEnd, root.val);
    int leftTreeNum = inorderRootIndex - inStart;

    root.left = buildTree(preorder, preStart+1, preStart+leftTreeNum, inorder, inStart, inorderRootIndex-1);
    root.right = buildTree(preorder, preStart+leftTreeNum+1, preEnd, inorder, inorderRootIndex+1, inEnd);

    return root;
}


```


