
function FileSizeUtils() {
    let unitSize = 1000;
    let byteSize = 1;
    let kiloSize = byteSize * unitSize;
    let megaSize = kiloSize * unitSize;
    let gigaSize = megaSize * unitSize;
    let teraSize = gigaSize * unitSize;
    let petaSize = teraSize * unitSize;
    let exaSize = petaSize * unitSize;

    let bytePostfix = "B";
    let kiloPostfix = "KB";
    let megaPostfix = "MB";
    let gigaPostfix = "GB";
    let teraPostfix = "TB";
    let petaPostfix = "PB";
    let exaPostfix = "EB";


    this.toHumanUnit = function(size) {
        if (size == null) {
            return bytePostfix;
        }
        if (size >= exaSize) {
            return exaPostfix;
        } else if (size >= petaSize) {
            return petaPostfix;
        } else if (size >= teraSize) {
            return teraPostfix;
        } else if (size >= gigaSize) {
            return gigaPostfix;
        } else if (size >= megaSize) {
            return megaPostfix;
        } else if (size >= kiloSize) {
            return kiloPostfix;
        } else {
            return bytePostfix;
        }
    }

    this.getByteSize = function(unit) {
        if (exaPostfix === unit) {
            return exaSize;
        } else if (petaPostfix === unit) {
            return petaSize;
        } else if (teraPostfix === unit) {
            return teraSize;
        } else if (petaPostfix === unit) {
            return petaSize;
        } else if (gigaPostfix === unit) {
            return gigaSize;
        } else if (megaPostfix === unit) {
            return megaSize;
        } else if (kiloPostfix === unit) {
            return kiloSize;
        } else if (bytePostfix === unit) {
            return byteSize;
        } else {
            return byteSize;
        }
    }
}
