# 在CentOS上构建Sonic

最近在尝试一些轻量级的搜索引擎，看到Sonic这个项目，又是Rust写的。正好拿过来研究一下。
不过下载后构建出现了各种问题，所以在这里记录一下。
一般的Rust项目都通过cargo来管理依赖和构建，比较傻瓜式。
不过sonic项目使用到了rocksdb并通过clang进行编译，所以需要安装clang。

```bash
# 翻墙
export http_proxy=socks5://127.0.0.1:1080
export https_proxy=socks5://127.0.0.1:1080
# 卸载旧版gcc并安装新版本
sudo yum remove -y gcc
sudo yum install -y centos-release-scl
sudo yum install -y devtoolset-7-gcc*
# 该命令最好加入~/.bashrc
scl enable devtoolset-7 bash
# 卸载旧版cmake并安装新版本
sudo yum remove -y cmake
curl -kLO https://github.com/Kitware/CMake/releases/download/v3.15.0/cmake-3.15.0.tar.gz
tar -zxf cmake-3.15.0.tar.gz
cd cmake-3.15.0
./bootstrap --prefix=/usr
make
sudo make install
# 安装llvm
LLVM_VERSION=8.0.1
# libffi
sudo yum install -y libffi
# llvm
curl -kLO https://github.com/llvm/llvm-project/releases/download/llvmorg-${LLVM_VERSION}/llvm-${LLVM_VERSION}.src.tar.xz
tar -Jxf llvm-${LLVM_VERSION}.src.tar.xz
# clang
curl -kLO https://github.com/llvm/llvm-project/releases/download/llvmorg-${LLVM_VERSION}/cfe-${LLVM_VERSION}.src.tar.xz
# runtime
curl -kLO https://github.com/llvm/llvm-project/releases/download/llvmorg-${LLVM_VERSION}/compiler-rt-${LLVM_VERSION}.src.tar.xz
tar -Jxf compiler-rt-${LLVM_VERSION}.src.tar.xz 
# initialize builds
mkdir -p llvm-${LLVM_VERSION}.build
cd llvm-${LLVM_VERSION}.build
mkdir -p tools && tar -xf ../cfe-${LLVM_VERSION}.src.tar.xz -C tools
mv tools/cfe-${LLVM_VERSION}.src tools/clang
mkdir -p projects && tar -xf ../compiler-rt-${LLVM_VERSION}.src.tar.xz -C projects
mv projects/compiler-rt-${LLVM_VERSION}.src projects/compiler-rt

CC=gcc CXX=g++ \
cmake -G 'Unix Makefiles' \
-DCMAKE_INSTALL_PREFIX=/usr \
-DLLVM_ENABLE_FFI=ON \
-DCMAKE_BUILD_TYPE=Release \
-DLLVM_BUILD_LLVM_DYLIB=ON \
-DLLVM_LINK_LLVM_DYLIB=ON \
-DLLVM_ENABLE_RTTI=ON \
-DLLVM_TARGETS_TO_BUILD="host;AMDGPU;BPF" \
-DLLVM_BUILD_TESTS=ON \
-Wno-dev \
../llvm-${LLVM_VERSION}.src
# 漫长的编译
make
sudo make install

cd ..
# 安装z3
curl -kLO https://github.com/Z3Prover/z3/archive/Z3-4.8.5.tar.gz
tar -zxf Z3-4.8.5.tar.gz
cd z3-Z3-4.8.5/
python scripts/mk_make.py
cd build
make
sudo make install

cd ../..
# 安装clang
tar -xf cfe-${LLVM_VERSION}.src.tar.xz
mkdir -p cfe-${LLVM_VERSION}.build
cd cfe-${LLVM_VERSION}.build
CC=gcc CXX=g++ \
cmake -G 'Unix Makefiles' \
-DCMAKE_INSTALL_PREFIX=/usr \
-DCMAKE_BUILD_TYPE=Release \
-DLLVM_LINK_LLVM_DYLIB=ON \
-DLLVM_ENABLE_RTTI=ON \
-DLLVM_TARGETS_TO_BUILD="host;AMDGPU;BPF" \
-Wno-dev \
../cfe-${LLVM_VERSION}.src
make
sudo make install

cd ..
# 安装runtime
tar -xf compiler-rt-${LLVM_VERSION}.src.tar.xz
mkdir -p compiler-rt-${LLVM_VERSION}.build
cd compiler-rt-${LLVM_VERSION}.build
CC=gcc CXX=g++ \
cmake -G 'Unix Makefiles' \
-DCMAKE_INSTALL_PREFIX=/usr \
-DCMAKE_BUILD_TYPE=Release \
-DLLVM_LINK_LLVM_DYLIB=ON \
-DLLVM_ENABLE_RTTI=ON \
-DLLVM_TARGETS_TO_BUILD="host;AMDGPU;BPF" \
-Wno-dev \
../compiler-rt-${LLVM_VERSION}.src
make
sudo make install

# 愉快编译sonic
git clone https://github.com/valeriansaliou/sonic.git
cd sonic
cargo build --release
```