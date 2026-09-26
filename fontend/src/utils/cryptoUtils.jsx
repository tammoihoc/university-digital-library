// src/utils/cryptoUtils.jsx

const PRIVATE_KEY_PEM = `-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDGeG9upc5RA6M1
CM0YHB6QNH7BHoLgCe3sIJNOOi1f3OLlxPI9BikYfSJAw6CcV/92GqQfopy4oxd1
2282zW9/Sgsm9WWq832oeo3sHEYssD4nVViiEzFr8Ce/5TfiKJu8rsmoNTul0kqF
1v7Nc75799vEa5wm9nzKpLF3hnVO7Cxyy+RiPLenp1l5n9u6iyjvKYvwluA4f+oh
ycFlHy6i2P+70U0RFVz4KwkY1X7Ihhyz3CX7cEE5xurasPJgR1rI/LJTmRT5sq14
SfZ1VtrifXe6FYEdE20qbmK4SDT9zz7D0f58HxJRuqGr/ICx3/IQ6Gt3af7ucc8P
UTANuS3fAgMBAAECggEAYnVc0odu0sH7NVQoas2IXAGu4B9CzeIfJgUDDsvNtsDd
zx7lDWEBAiUR2Q8znlwJX43X3dnN5csC2HUDtywzI/KXlbwns6cXr30c6wUbyw+j
xd+RGNZsrh91mL0d/BQpLnGHmOrPrHOmhL8jqMjCtr0/STIZRTsCrYUGhUUu7Pdc
54Q8gnzzk22LF1snT6hUHzQxFdzE/kdAQZ447F7IhJguQYbtFn1ZCH3YaEmGwzOZ
j7TtXh4NatqcZwC1IKFwLnCtkFWZzxzquU9ztxSdiGAkIOW/o/eGPGK6JjCuo+DH
iFSUph5Cq3jpzT64f8XqeW21dfUtcR3+vNB9cBnIAQKBgQDxhsuBe/kE2kS6MR1k
lL278+uZRdBvX6bg75qwDXLvQMhAIR/yH3grYh8P5kIonnBK2NFbfidmwtLSEsfX
KsnevF72LMax2qvL6lnWNiscpcGxFrp1UzfojjFOAyqjoMKBKGCo7TsqbqJTpI+M
9f9obXLJ8o3vhdiQViHn9vGJmwKBgQDSXSBayJVqGzwVKnnos0kcy85zF4Q2rmux
cO+dWoKCIMzSDCnu3qJc4PQHgvAxx9yl/LEc/C/RUtvZxqxpj4RoRU1jc2U0eEv4
DnzfsP66/D90/fyNuk4VVd5qTnqh1N2IXr+4Xx6LlwkE6N7L70qJI8XkQwMWqoY4
+A6SKb8jDQKBgQCt4NnvbR3YRX7HAIblm0OA5VjOrnkhRR5xv22Aox4EaoH/0Rkb
iVZM/UXZf25PqcizyaXnFUoua8G0pPqjx9Uu+jPvTEr7Ta7yjdOBKNwesqJf6Dny
LygHzx5lyFfRFvIQY846rxcyNBW+27DQzWTDfE/wXSObtaM0Ph4IzjoTUwKBgFKo
Iulo/USW9PHAIeysXaMB9dnFOL8fo9Mx9ATZJoSpDLHP874XHqbuARTefzCgPnO+
KX2hHczbCOW2KdLEgJtT98eG9RI73mXWk3x1mHGyYeC/V7f8p2e8uMr+kTL7aByI
Vj7EqUTiELosIjbxjFD8jGpXZmRGlrzVwyvtWVwVAoGBALahzGOLBb38gH6qkCYL
7FvWazJHyEoPRAYA2nB43HW38SYKlEPFZ8AcDVhNozqgFxl6R+3sjxqfRJ+nqueX
3ttD2FO7vMrLsbsqSwvwumrPWZrNkA2oqXQUKUe3h56+ZeYMCoiBFVoXWNZkgQtF
vGEtP3iPIKLD0W6NpOklyRQ8
-----END PRIVATE KEY-----`;

function base64ToArrayBuffer(base64) {
  const binaryString = window.atob(base64);
  const len = binaryString.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes.buffer;
}

function arrayBufferToBase64(buffer) {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return window.btoa(binary);
}

let importedPrivateKeyPromise = null;

async function getPrivateKey() {
  if (!importedPrivateKeyPromise) {
    importedPrivateKeyPromise = (async () => {
      try {
        const cleanKey = PRIVATE_KEY_PEM
          .replace('-----BEGIN PRIVATE KEY-----', '')
          .replace('-----END PRIVATE KEY-----', '')
          .replace(/\s+/g, '');
        
        const keyBuffer = base64ToArrayBuffer(cleanKey);
        
        return await window.crypto.subtle.importKey(
          'pkcs8',
          keyBuffer,
          {
            name: 'RSASSA-PKCS1-v1_5',
            hash: { name: 'SHA-256' },
          },
          false,
          ['sign']
        );
      } catch (err) {
        console.error('Failed to import private key for request signing:', err);
        throw err;
      }
    })();
  }
  return importedPrivateKeyPromise;
}

export async function generateSignatureHeaders(method, path) {
  try {
    const privateKey = await getPrivateKey();
    const timestamp = Date.now().toString();
    const nonce = Math.random().toString(36).substring(2) + Date.now().toString(36);
    
    // Canonical string: METHOD:PATH:TIMESTAMP:NONCE
    const canonicalString = `${method.toUpperCase()}:${path}:${timestamp}:${nonce}`;
    
    const encoder = new TextEncoder();
    const dataBuffer = encoder.encode(canonicalString);
    
    const signatureBuffer = await window.crypto.subtle.sign(
      'RSASSA-PKCS1-v1_5',
      privateKey,
      dataBuffer
    );
    
    const signatureBase64 = arrayBufferToBase64(signatureBuffer);
    
    return {
      'X-Signature': signatureBase64,
      'X-Timestamp': timestamp,
      'X-Nonce': nonce
    };
  } catch (error) {
    console.error('Error generating signature headers:', error);
    return {}; // Trả về rỗng nếu có lỗi
  }
}
