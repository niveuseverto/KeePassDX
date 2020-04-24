/*
 * Copyright 2017 Brian Pellin, Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePassDX.
 *
 *  KeePassDX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.crypto.finalkey

import com.kunzisoft.keepass.crypto.CipherFactory.deviceBlacklisted

object AESKeyTransformerFactory : KeyTransformer() {
    override fun transformMasterKey(seed: ByteArray?, key: ByteArray?, rounds: Long): ByteArray? {
        // Prefer the native final key implementation
        val keyTransformer = if (!deviceBlacklisted()
                && NativeAESKeyTransformer.available()) {
            NativeAESKeyTransformer()
        } else {
            // Fall back on the android crypto implementation
            AndroidAESKeyTransformer()
        }
        return keyTransformer.transformMasterKey(seed, key, rounds)
    }
}