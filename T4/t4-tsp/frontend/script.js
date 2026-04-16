const canvas = document.getElementById('canvas');
const ctx = canvas.getContext('2d');

let scaleFactor = 1;
let nearestProgress = 0;
let smallestProgress = 0;
let animFrame;

function init() {
    if (animFrame) {
        cancelAnimationFrame(animFrame);
    }
    if (typeof tspData === 'undefined') {
        alert("Aguardando exportação offline do Java...");
        return;
    }

    document.getElementById('pts').textContent = tspData.points.length.toLocaleString('pt-BR');
    document.getElementById('n-len').textContent = tspData.nearestLength > 0 ? tspData.nearestLength.toLocaleString('pt-BR', {maximumFractionDigits: 2}) : "PULADO";
    document.getElementById('s-len').textContent = tspData.smallestLength > 0 ? tspData.smallestLength.toLocaleString('pt-BR', {maximumFractionDigits: 2}) : "PULADO";
    
    document.getElementById('n-time').textContent = `(${tspData.timeNearest || 0}ms)`;
    document.getElementById('s-time').textContent = `(${tspData.timeSmallest || 0}ms)`;
    
    const container = document.querySelector('.canvas-container');
    const padding = 60;
    const cw = container.clientWidth - padding;
    const ch = container.clientHeight - padding; 
    
    const showBoth = (tspData.nearestTour.length > 0 && tspData.smallestTour.length > 0);
    const availableWidth = showBoth ? cw / 2 : cw;
    
    const scaleX = availableWidth / (tspData.width || 600);
    const scaleY = ch / (tspData.height || 600);
    scaleFactor = Math.min(scaleX, scaleY);
    
    canvas.width = showBoth ? (tspData.width || 600) * scaleFactor * 2 : (tspData.width || 600) * scaleFactor;
    canvas.height = (tspData.height || 600) * scaleFactor;
    
    animFrame = requestAnimationFrame(renderLoop);
}

function tX(x) { return (x * scaleFactor); }
function tY(y) { return canvas.height - (y * scaleFactor); }

function drawScene(tourName, progress, colorHex) {
    // Desenha cidades 
    ctx.fillStyle = 'rgba(255, 255, 255, 0.4)';
    for (let p of tspData.points) {
        ctx.beginPath();
        ctx.arc(tX(p[0]), tY(p[1]), 1.5, 0, Math.PI * 2);
        ctx.fill();
    }

    const tour = tourName === 'nearest' ? tspData.nearestTour : tspData.smallestTour;
    
    if (tour && tour.length > 0) {
        let limit = Math.min(progress, tour.length);
        
        ctx.strokeStyle = colorHex;
        ctx.lineWidth = 1.5;
        ctx.shadowBlur = 12;
        ctx.shadowColor = colorHex;
        ctx.beginPath();
        
        for (let i = 0; i < limit; i++) {
            const p = tour[i];
            if (i === 0) ctx.moveTo(tX(p[0]), tY(p[1]));
            else ctx.lineTo(tX(p[0]), tY(p[1]));
        }
        ctx.stroke();
    }
}

function renderLoop() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const speed = Math.max(1, Math.floor(tspData.points.length / 50)); 
    let stillAnimating = false;

    const showBoth = (tspData.nearestTour.length > 0 && tspData.smallestTour.length > 0);
    const offsetX = showBoth ? canvas.width / 2 : 0;

    // 1) QUADRANTE ESQUERDO: Nearest
    if (tspData.nearestTour && tspData.nearestTour.length > 0) {
        if (nearestProgress < tspData.nearestTour.length) {
            nearestProgress += speed;
            stillAnimating = true;
        }
        
        ctx.save();
        drawScene('nearest', nearestProgress, '#fb7185');
        
        ctx.shadowBlur = 0;
        ctx.fillStyle = "#fff";
        ctx.font = "bold 20px Outfit, sans-serif";
        ctx.fillText("🔴 Nearest Insertion", 20, 30);
        ctx.restore();
    }

    // 2) QUADRANTE DIREITO: Smallest
    if (tspData.smallestTour && tspData.smallestTour.length > 0) {
        if (smallestProgress < tspData.smallestTour.length) {
            smallestProgress += speed;
            stillAnimating = true;
        }
        
        ctx.save();
        if (showBoth) {
            ctx.translate(offsetX, 0);
            ctx.strokeStyle = "rgba(255,255,255,0.1)";
            ctx.beginPath(); ctx.moveTo(0, 0); ctx.lineTo(0, canvas.height); ctx.stroke();
        }
        drawScene('smallest', smallestProgress, '#38bdf8');
        
        ctx.shadowBlur = 0;
        ctx.fillStyle = "#fff";
        ctx.font = "bold 20px Outfit, sans-serif";
        ctx.fillText("🔵 Smallest Insertion", 20, 30);
        ctx.restore();
    }

    if (stillAnimating) {
        animFrame = requestAnimationFrame(renderLoop);
    } else {
        finishOffLines(showBoth, offsetX);
    }
}

function finishOffLines(showBoth, offsetX) {
    if(tspData.nearestTour.length > 0) {
         ctx.save();
         ctx.strokeStyle = '#fb7185'; ctx.lineWidth = 1.5; ctx.shadowBlur = 10; ctx.shadowColor = '#fb7185'; ctx.beginPath();
         let pLast = tspData.nearestTour[tspData.nearestTour.length-1], pFirst = tspData.nearestTour[0];
         ctx.moveTo(tX(pLast[0]), tY(pLast[1])); ctx.lineTo(tX(pFirst[0]), tY(pFirst[1])); ctx.stroke();
         ctx.restore();
    }
    
    if(tspData.smallestTour.length > 0) {
         ctx.save();
         if(showBoth) ctx.translate(offsetX, 0);
         ctx.strokeStyle = '#38bdf8'; ctx.lineWidth = 1.5; ctx.shadowBlur = 10; ctx.shadowColor = '#38bdf8'; ctx.beginPath();
         let pLast = tspData.smallestTour[tspData.smallestTour.length-1], pFirst = tspData.smallestTour[0];
         ctx.moveTo(tX(pLast[0]), tY(pLast[1])); ctx.lineTo(tX(pFirst[0]), tY(pFirst[1])); ctx.stroke();
         ctx.restore();
    }
}

window.addEventListener('resize', () => { nearestProgress = 0; smallestProgress = 0; init(); });

try { init(); } catch(e){}


const fileInput = document.getElementById('fileInput');

fileInput.addEventListener('change', function(e) {
    try {
        const file = e.target.files[0];
        if (!file) return;
        
        document.getElementById('pts').textContent = "Processando arquivo...";
        
        const reader = new FileReader();
        reader.onerror = function() {
            alert("Erro ao ler o arquivo no navegador.");
        };
        reader.onload = function(evt) {
            try {
                const text = evt.target.result;
                const lines = text.split('\n');
                
                let dimsFound = false;
                let w = 800, h = 800;
                
                const pts = [];
                for(let i=0; i<lines.length; i++) {
                    const line = lines[i].trim();
                    if(!line) continue;
                    const coords = line.split(/\s+/);
                    
                    if (!dimsFound && coords.length === 2 && i === 0) {
                        w = parseInt(coords[0]) || 800;
                        h = parseInt(coords[1]) || 800;
                        dimsFound = true;
                        continue;
                    }
                    
                    if(coords.length >= 2) {
                        pts.push([parseFloat(coords[0]), parseFloat(coords[1])]);
                    }
                }
                
                if (pts.length === 0) {
                    alert('Nenhum ponto válido encontrado no TXT!'); 
                    document.getElementById('pts').textContent = "Erro de Tópicos";
                    return;
                }
                
                document.getElementById('pts').textContent = "Processando heurísticas (" + pts.length + " pontos)...";
                setTimeout(() => {
                    try {
                        let startN = performance.now();
                        let tNearest = computeNearest(pts);
                        let timeN = Math.round(performance.now() - startN);

                        let startS = performance.now();
                        let tSmallest = computeSmallest(pts);
                        let timeS = Math.round(performance.now() - startS);
                        
                       
                        tspData.width = w;
                        tspData.height = h;
                        tspData.points = pts;
                        tspData.nearestTour = tNearest;
                        tspData.nearestLength = calculateLength(tNearest);
                        tspData.timeNearest = timeN;
                        tspData.smallestTour = tSmallest;
                        tspData.smallestLength = calculateLength(tSmallest);
                        tspData.timeSmallest = timeS;
                        
                        nearestProgress = 0; 
                        smallestProgress = 0;
                        
                        ctx.clearRect(0, 0, canvas.width, canvas.height);
                        init();
                    } catch (ex) {
                        alert("Falha ao calcular heurísticas em JS: " + ex.message);
                        document.getElementById('pts').textContent = "Erro Matemático: " + ex.message;
                    }
                }, 50);
                
                fileInput.value = "";
                
            } catch (ex) {
                alert("Falha lógica após leitura: " + ex.message);
                document.getElementById('pts').textContent = "Erro: " + ex.message;
            }
        };
        reader.readAsText(file);
    } catch(err) {
        alert("Erro no seletor de arquivo: " + err.message);
    }
});

function distance(p1, p2) { return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2)); }
function calculateLength(tour) {
    if(tour.length === 0) return 0;
    let dist = 0;
    for(let i=0; i<tour.length; i++) dist += distance(tour[i], tour[(i+1)%tour.length]);
    return dist;
}
function computeNearest(pts) {
    if(pts.length === 0) return [];
    let tour = [pts[0]]; 
    for (let i = 1; i < pts.length; i++) {
        let bestIndex = 0, minDistance = Infinity;
        for (let j = 0; j < tour.length; j++) {
            let dist = distance(tour[j], pts[i]);
            if (dist < minDistance) { minDistance = dist; bestIndex = j; }
        }
        tour.splice(bestIndex + 1, 0, pts[i]);
    }
    return tour;
}
function computeSmallest(pts) {
    if(pts.length === 0) return [];
    let tour = [pts[0]];
    for (let i = 1; i < pts.length; i++) {
        let bestIndex = 0, minIncrease = Infinity;
        for (let j = 0; j < tour.length; j++) {
            let increase = distance(tour[j], pts[i]) + distance(pts[i], tour[(j+1)%tour.length]) - distance(tour[j], tour[(j+1)%tour.length]);
            if (increase < minIncrease) { minIncrease = increase; bestIndex = j; }
        }
        tour.splice(bestIndex + 1, 0, pts[i]);
    }
    return tour;
}
