.class public Main
.super java/lang/Object
.method public <init>()V
    .limit stack 80
    .limit locals 80
    0: aload_0
    1: invokespecial java/lang/Object/<init>()V
    4: return
.end method

.method static calculate()I
    .limit stack 80
    .limit locals 80
    bipush 12
    iconst_5
    bipush 15
    irem
    iconst_1
    iconst_2
    iconst_1
    idiv
    imul
    iconst_2
    iadd
    iconst_2
    imul
    iadd
    idiv
    bipush -1
    isub
    istore_0
    iload_0
    iconst_2
    iload_0
    bipush 10
    irem
    iadd
    bipush 12
    bipush 8
    iconst_3
    iload_0
    irem
    isub
    iconst_1
    idiv
    idiv
    iconst_2
    isub
    isub
    isub
    istore_1
    iload_1
    iload_0
    iconst_3
    irem
    iload_1
    iconst_3
    iconst_1
    bipush 9
    bipush 7
    iconst_5
    irem
    idiv
    bipush 10
    bipush 9
    isub
    isub
    isub
    iconst_2
    iadd
    isub
    iconst_1
    imul
    idiv
    isub
    bipush 8
    isub
    imul
    istore_2
    iload_2
    iload_0
    bipush 31
    iconst_1
    iload_0
    bipush -2
    idiv
    iload_1
    iconst_2
    imul
    isub
    iload_0
    idiv
    iload_2
    iadd
    isub
    imul
    irem
    bipush 12
    isub
    isub
    istore_3
    iload_3
    bipush 10
    irem
    bipush 40
    iconst_2
    irem
    isub
    bipush 20
    irem
    istore_3
    iload_0
    iload_2
    bipush 32
    irem
    bipush 9
    iload_1
    imul
    bipush 23
    iload_2
    idiv
    iadd
    isub
    isub
    bipush 9
    irem
    iload_0
    iconst_3
    iload_1
    iconst_2
    iload_3
    bipush 24
    isub
    imul
    irem
    bipush 30
    iconst_3
    iconst_2
    imul
    idiv
    iadd
    iload_2
    idiv
    isub
    iconst_4
    iconst_2
    irem
    iadd
    iadd
    isub
    istore 4
    iload 4
    ireturn
.end method

.method public static main([Ljava/lang/String;)V
    .limit stack 80
    .limit locals 80
    invokestatic Main/calculate()I
    istore_1
    getstatic java/lang/System/out Ljava/io/PrintStream;
   iload_1
   invokevirtual java/io/PrintStream/println(I)V
    return
.end method

