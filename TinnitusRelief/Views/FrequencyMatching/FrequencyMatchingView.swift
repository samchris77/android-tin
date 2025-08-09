import SwiftUI

struct FrequencyMatchingView: View {
    @StateObject private var viewModel = FrequencyMatchingViewModel()
    @Binding var selectedTab: Int
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                backgroundView
                dotGridOverlay
                controlSurface(geometry: geometry)
                frequencyVolumeDisplay
            }
        }
        .navigationBarHidden(true)
    }
    
    private var backgroundView: some View {
        LinearGradient(
            colors: [
                Color.gray.opacity(0.15),
                Color.orange.opacity(0.25),
                Color.red.opacity(0.15)
            ],
            startPoint: .top,
            endPoint: .bottom
        )
        .ignoresSafeArea()
    }
    
    private var dotGridOverlay: some View {
        Canvas { context, size in
            let spacing: CGFloat = 25
            let dotSize: CGFloat = 1.5
            
            for x in stride(from: 0, through: size.width, by: spacing) {
                for y in stride(from: 0, through: size.height, by: spacing) {
                    let rect = CGRect(x: x - dotSize/2, y: y - dotSize/2, width: dotSize, height: dotSize)
                    context.fill(
                        Path(ellipseIn: rect),
                        with: .color(Color.white.opacity(0.15))
                    )
                }
            }
        }
        .ignoresSafeArea()
    }
    
    private func controlSurface(geometry: GeometryProxy) -> some View {
        let safeArea = geometry.safeAreaInsets
        let availableHeight = geometry.size.height - safeArea.top - safeArea.bottom - 120
        let availableWidth = geometry.size.width - 60
        let controlAreaFrame = CGRect(
            x: 30,
            y: safeArea.top + 80,
            width: availableWidth,
            height: availableHeight
        )
        
        return ZStack {
            controlLabels(frame: controlAreaFrame)
            
            FrequencyControlPoint(isDragging: viewModel.isDragging)
                .position(
                    x: controlAreaFrame.minX + controlAreaFrame.width * viewModel.controlPosition.x,
                    y: controlAreaFrame.minY + controlAreaFrame.height * viewModel.controlPosition.y
                )
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            if !viewModel.isDragging {
                                viewModel.startDragging()
                            }
                            
                            let newPosition = CGPoint(
                                x: value.location.x - controlAreaFrame.minX,
                                y: value.location.y - controlAreaFrame.minY
                            )
                            
                            viewModel.updateControlPosition(
                                to: newPosition,
                                in: CGSize(width: controlAreaFrame.width, height: controlAreaFrame.height)
                            )
                        }
                        .onEnded { _ in
                            viewModel.stopDragging()
                        }
                )
        }
    }
    
    private func controlLabels(frame: CGRect) -> some View {
        ZStack {
            Text("louder")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.orange)
                .position(x: frame.midX, y: frame.minY - 10)
            
            Text("softer")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.orange)
                .position(x: frame.midX, y: frame.maxY + 20)
            
            Text("bass")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.orange)
                .rotationEffect(.degrees(-90))
                .position(x: frame.minX - 20, y: frame.midY)
            
            Text("treble")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.orange)
                .rotationEffect(.degrees(90))
                .position(x: frame.maxX + 20, y: frame.midY)
        }
    }
    
    
    private var frequencyVolumeDisplay: some View {
        VStack {
            Spacer()
            
            VStack(spacing: 12) {
                HStack(spacing: 24) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Frequency")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(viewModel.getFrequencyDisplayText())
                            .font(.system(size: 16, weight: .semibold, design: .monospaced))
                            .foregroundColor(.primary)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Volume")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text(viewModel.getVolumeDisplayText())
                            .font(.system(size: 16, weight: .semibold, design: .monospaced))
                            .foregroundColor(.primary)
                    }
                    
                    Spacer()
                    
                    Button(action: {
                        if viewModel.isPlaying {
                            viewModel.stopPlaying()
                        } else {
                            viewModel.startPlaying()
                        }
                    }) {
                        HStack(spacing: 6) {
                            Image(systemName: viewModel.isPlaying ? "pause.fill" : "play.fill")
                                .font(.system(size: 14, weight: .medium))
                            Text(viewModel.isPlaying ? "Pause" : "Play")
                                .font(.system(size: 14, weight: .medium))
                        }
                        .foregroundColor(.white)
                        .frame(height: 38)
                        .padding(.horizontal, 16)
                        .background(
                            LinearGradient(
                                colors: viewModel.isPlaying ? [Color.red, Color.orange] : [Color.orange, Color.red],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .cornerRadius(19)
                        .shadow(radius: 2, x: 0, y: 1)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 16)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
                    .padding(.horizontal, 8)
                    .shadow(radius: 4, x: 0, y: -2)
            )
            .padding(.bottom, 20)
        }
    }
}

#Preview {
    FrequencyMatchingView(selectedTab: .constant(1))
}