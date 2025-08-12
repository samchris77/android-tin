import Foundation

protocol AudioManagerDelegate: AnyObject {
    func audioManagerDidUpdateFrequency(_ frequency: Float)
    func audioManagerDidUpdateVolume(_ volume: Float) 
    func audioManagerDidUpdatePlayingState(_ isPlaying: Bool)
}