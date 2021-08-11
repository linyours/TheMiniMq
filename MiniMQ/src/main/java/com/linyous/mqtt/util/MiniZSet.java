package com.linyous.mqtt.util;

import java.util.Random;

/**
 * @author Linyous
 * @date 2021/6/8 16:22
 */
public class MiniZSet<T> {

    private SkipList<T> skipList;

    public MiniZSet() {
        this.skipList = new SkipList<T>();
    }

    public boolean contain(long score) {
        return skipList.contain(score);
    }

    public T get(long score) {
        return (T) skipList.find(score).value;
    }

    //插入节点
    public void insert(long score, T value) {
        skipList.insert(score, value);
    }

    //通过分值移除某一个节点
    public boolean remove(long score) {
        return skipList.remove(score);
    }

    //范围删除,包括startScore的节点
    public void rangeRemove(long startScore) {
        skipList.rangeRemove(startScore);
    }

    //范围删除,包括startScore的节点,包括endScore的节点
    public void rangeRemove(long startScore, long endScore) {
        skipList.rangeRemove(startScore, endScore);
    }

    //包括startScore之后的节点数目
    public int getNodeCount(long startScore) {
        return skipList.getNodeCount(startScore);
    }

    //获取包括startScore的节点,包括endScore的节点的节点数目
    public int getNodeCount(long startScore, long endScore) {
        return skipList.getNodeCount(startScore, endScore);
    }

    //打印最底层的所有节点
    public void printAll() {
        skipList.printAll();
    }

    private class SkipList<T> {
        // 最上层链表的头指针
        private Node<T> head;
        // 最上层聊表的尾指针
        private Node<T> tail;
        // 跳表的层数
        private int level;
        // 插入链表元素的个数
        private int size;
        // 用来生成随机数
        private Random random;

        public SkipList() {
            this.head = new Node(Long.MIN_VALUE, null);
            this.tail = new Node(Long.MAX_VALUE, null);
            this.level = 1;
            this.size = 0;
            this.random = new Random();
            this.head.right = this.tail;
            this.tail.left = this.head;
        }

        //判断跳表中是否有指定score的节点
        public boolean contain(long score) {
            Node help = head;
            while (help != null) {
                if (help.score == score) {
                    return true;
                } else if (help.right.score > score) {
                    help = help.down;
                } else if (help.right.score <= score) {
                    help = head.right;
                }
            }
            return false;
        }

        //返回指定score的元素
        public Node find(long score) {
            Node help = head;
            while (help != null) {
                if (help.score == score) {
                    return help;
                } else if (help.right.score > score) {
                    help = help.down;
                } else if (help.right.score <= score) {
                    help = head.right;
                }
            }
            return null;
        }

        //在最底层，找到指定score节点的前面一个节点,
        public Node findPreNode(long score) {
            //拿到最底层的head指针,从头开始判断最底部的链表，效率不行
            //Node help = headToButtom();
            Node help = head;
            //开始查询指定score节点的前面一个节点。如果链表中有指定score的节点，则返回相同节点的前面一个节点
            while (true) {
                if (score <= help.right.score) {
                    if (help.down == null) return help;
                    else help = help.down;
                } else {
                    help = help.right;
                }
            }
        }

        //返回最底层的head指针
        private Node headToButtom() {
            Node help = head;
            while (help.down != null) {
                help = help.down;
            }
            return help;
        }

        //判断跳表是否为空
        public boolean isEmpty() {
            return size != 0;
        }

        //打印最底层的所有节点
        public void printAll() {
            Node help = headToButtom();
            while (help != null) {
                System.out.println(help);
                help = help.right;
            }
        }

        //插入节点
        public void insert(long score, T value) {
            //找到目标位置的前一个节点
            Node pre = findPreNode(score);
            //判断后面节点是否是和要插入节点一样的score
            if (pre.right.score == score) {
                pre = pre.right;
                while (pre != null) {
                    pre.value = value;
                    pre = pre.up;
                }
                return;
            }
            //插入节点
            Node target = new Node(score, value);
            target.left = pre;
            target.right = pre.right;
            pre.right.left = target;
            pre.right = target;
            //当前所属的层级
            int currLevel = 1;
            //随机往上沿升
            while (random.nextDouble() > 0.5) {
                currLevel++;
                if (currLevel <= level) {
                    //不用再最上层生成一个新的链表
                    Node upNode = new Node(score, value);
                    Node right = target.right;
                    while (right.up == null) {
                        right = right.right;
                    }
                    right = right.up;
                    Node left = target.left;
                    while (left.up == null) {
                        left = left.left;
                    }
                    left = left.up;
                    upNode.left = left;
                    left.right = upNode;
                    upNode.right = right;
                    right.left = upNode;
                    target = upNode;
                } else {
                    //需要在最上方生成一个新的链表
                    this.level++;
                    Node upNode = new Node(score, value);
                    Node upHead = new Node(Long.MIN_VALUE, null);
                    Node upTail = new Node(Long.MAX_VALUE, null);
                    upHead.right = upNode;
                    upNode.left = upHead;
                    upTail.left = upNode;
                    upNode.right = upTail;
                    target = upNode;
                    upHead.down = this.head;
                    this.head.up = upHead;
                    this.head = upHead;
                    upTail.down = this.tail;
                    this.tail.up = upTail;
                    this.tail = upTail;
                }
            }
        }

        //通过分值移除某一个节点
        public boolean remove(long score) {
            //先找到最底层的节点
            Node target = find(score);
            if (target == null) {
                return false;
            } else {
                while (target != null) {
                    target.left.right = target.right;
                    target.right.left = target.left;
                    target = target.up;
                }
                return true;
            }
        }

        //移除某一个节点
        public void remove(Node node) {
            while (node != null) {
                node.left.right = node.right;
                node.right.left = node.left;
                node = node.up;
            }
        }

        //范围删除,包括startScore的节点
        public void rangeRemove(long startScore) {
            //找到目标位置的前一个节点
            Node pre = findPreNode(startScore);
            while (pre.right.score < Long.MAX_VALUE) {
                remove(pre.right);
            }
        }

        //范围删除,包括startScore的节点,包括endScore的节点
        public void rangeRemove(long startScore, long endScore) {
            //找到目标位置的前一个节点
            Node pre = findPreNode(startScore);
            while (pre.right.score <= endScore) {
                remove(pre.right);
            }
        }

        //包括startScore之后的节点数目
        public int getNodeCount(long startScore) {
            int result = 0;
            //找到目标位置的前一个节点
            Node pre = findPreNode(startScore);
            while (pre.right.score < Long.MAX_VALUE) {
                result++;
                pre = pre.right;
            }
            return result;
        }

        //获取包括startScore的节点,包括endScore的节点的节点数目
        public int getNodeCount(long startScore, long endScore) {
            int result = 0;
            //找到目标位置的前一个节点
            Node pre = findPreNode(startScore);
            while (pre.right.score <= endScore) {
                result++;
                pre = pre.right;
            }
            return result;
        }

        private class Node<T> {
            //score不能为负数
            long score;
            T value;
            Node up, down, left, right;

            public Node(T value) {
                this.score = -1;
                this.value = value;
            }

            public Node(long score, T value) {
                this.score = score;
                this.value = value;
            }

            @Override
            public String toString() {
                return "Node{" +
                        "score=" + score +
                        ", value=" + value +
                        ", up=" + up +
                        ", down=" + down +
                        ", left=" + left +
                        ", right=" + right +
                        '}';
            }
        }
    }
}



