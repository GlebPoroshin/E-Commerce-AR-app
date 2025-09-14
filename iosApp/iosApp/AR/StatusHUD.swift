//
//  StatusHUD.swift
//  iosApp
//
//  Created by Глеб Порошин on 14.09.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import UIKit

final class StatusHUD: UILabel {
    override init(frame: CGRect) {
        super.init(frame: frame)
        textAlignment = .center
        numberOfLines = 2
        font = .preferredFont(forTextStyle: .callout)
        textColor = .white
        backgroundColor = UIColor(white: 0, alpha: 0.4)
        layer.cornerRadius = 10
        layer.masksToBounds = true
        translatesAutoresizingMaskIntoConstraints = false
        alpha = 0
    }

    required init?(coder: NSCoder) { fatalError() }

    func setMessage(_ message: String, visible: Bool = true) {
        text = "  " + message + "  "
        UIView.animate(withDuration: 0.15) {
            self.alpha = visible ? 1 : 0
        }
    }
}
