let idCounter = 0
const labels = []

const getAllLabels = () => labels

const getLabel = (id) => labels.find(a => a.id === id)

const createLabel = (title, content) => {
     const newLabel = { id: ++idCounter, title, content } 
     labels.push(newLabel) 
     return newLabel }

const updateLabel = (id, title, content) => {
     const label = getLabel(id) 
     if (!label) return null
     if (title) label.title = title
     if (content) label.content = content
     return label }

const deleteLabel = (id) => {
     const index = labels.findIndex(a => a.id === id)
     if (index === -1) return null
     return labels.splice(index, 1)[0] 
    }

module.exports = {
    getAllLabels,
    getLabel, 
    createLabel, 
    updateLabel, 
    deleteLabel 
}