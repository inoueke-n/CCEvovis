
function copyToClipboard1() {
    // コピー対象をJavaScript上で変数として定義する
    var copyTarget = document.getElementById("oldcommitID");
    // コピー対象のテキストを選択する
    copyTarget.select();
    // 選択しているテキストをクリップボードにコピーする
    document.execCommand("Copy");
    // コピーをお知らせする

}

function copyToClipboard2() {
    // コピー対象をJavaScript上で変数として定義する
    var copyTarget = document.getElementById("newcommitID");
    // コピー対象のテキストを選択する
    copyTarget.select();
    // 選択しているテキストをクリップボードにコピーする
    document.execCommand("Copy");
    // コピーをお知らせする

}
