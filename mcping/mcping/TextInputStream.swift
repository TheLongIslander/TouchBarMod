import Foundation

class TextInputStream {
    private let fileHandle: FileHandle
    private let encoding: String.Encoding
    private var buffer = Data()

    init(fileHandle: FileHandle, encoding: String.Encoding = .utf8) throws {
        self.fileHandle = fileHandle
        self.encoding = encoding
    }

    func readLine() -> String? {
        while true {
            if let range = buffer.range(of: Data([0x0A])) {
                let lineData = buffer.subdata(in: 0..<range.lowerBound)
                buffer.removeSubrange(0...range.lowerBound)
                return String(data: lineData, encoding: encoding)
            }

            let chunk = try? fileHandle.read(upToCount: 1024)
            if let chunk = chunk, !chunk.isEmpty {
                buffer.append(chunk)
            } else {
                return nil
            }
        }
    }
}
